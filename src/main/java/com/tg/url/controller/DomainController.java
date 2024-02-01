package com.tg.url.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tg.url.config.TgConstants;
import com.tg.url.service.DbConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Controller
public class DomainController {

    public static final Logger logger = LogManager.getLogger(DomainController.class);

    private final DbConnectionManager dbConnectionManager;
    private String savedFilePath = null;

    @Autowired
    public DomainController(DbConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }


    @GetMapping("/domain_upload")
    public String domainUpload() {
        return "domain_upload";
    }
    @PostMapping("/domain-upload")
    @ResponseBody
    public ResponseEntity handleFileUpload(@RequestParam("file") MultipartFile file) {
        // 파일 저장
        logger.info("handleFileUpload called");
        saveFile(file);

        try {
            upsertDomainInfo(); //insert data in db
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return ResponseEntity.ok(Collections.singletonMap("message", "uploadFail"));
        }

        return ResponseEntity.ok(Collections.singletonMap("message", "uploadSuccess"));
    }

    private void saveFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String filePath = TgConstants.DOMAIN_UPLOAD_PATH + "/" + fileName;
            file.transferTo(new File(filePath));
            logger.info("File saved done");
            savedFilePath = filePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean upsertDomainInfo() throws Exception {
        Connection conn = dbConnectionManager.getDataSource().getConnection();
        FileInputStream excelFile = new FileInputStream(savedFilePath);
        Workbook workbook = new XSSFWorkbook(excelFile);
        Sheet sheet = workbook.getSheetAt(0);

        List<String> domainSet = new ArrayList<>();
        for (Row row : sheet) {
            String rawDomain = row.getCell(1).getStringCellValue(); // second column value
            logger.info("raw domain string: {}", rawDomain);
            String formedDomain = rawDomain.replaceAll("http://", "")
                    .replaceAll("https://", "")
                    .replaceAll("www.", "");
            logger.info("formed domain string: {}", formedDomain);
            if(!domainSet.contains(formedDomain)) {
                domainSet.add(formedDomain);
            }
        }
        for(String domainStr : domainSet) {
            String sql = "INSERT INTO domain (domain) SELECT ? WHERE NOT EXISTS (SELECT domain FROM domain WHERE domain = ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, domainStr);
            pstmt.setString(2, domainStr);
            pstmt.execute();
            logger.info("domain string {} inserted", domainStr);
        }

        workbook.close();
        excelFile.close();
        return true;
    }

    @DeleteMapping("/domain_truncate")
    @ResponseBody
    public ResponseEntity<?> domainTruncate() {
        try (Connection conn = dbConnectionManager.getDataSource().getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("TRUNCATE TABLE domain");
            pstmt.execute();
            return ResponseEntity.ok(Collections.singletonMap("message", "Domain DB 삭제 완료"));
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "DB 삭제 실패"));
        }
    }

    @GetMapping("/domain_register") // 고객사 정보 입력, customer db에 없을 시 insert 하고, domain 1개 제안, 반환
    public String domainRegister() {
        return "domain_register";
    }

    @PostMapping("/domain-register")
    @ResponseBody
    public ResponseEntity<String> handleDomainRetrieve(@RequestParam("customer-name") String customerName) {
        // 파일 저장
        logger.info("CustomerRetrieve svc called");

        try {
            String customerId = retrieveCustomerInfo(customerName);
            if(customerId == null) {
                return ResponseEntity.ok("notFound");
            } else {
                // return customer's registered domain info
                JsonObject jo = retrieveRegisteredDomainInfo(customerName);

                // recommend domain
                JsonArray domainList = getDomain(customerName);
                jo.add("recommendedDomainList", domainList);
                Gson gson = new Gson();
                return ResponseEntity.ok(gson.toJson(jo));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return ResponseEntity.ok("fail");
        }
    }

    private JsonArray getDomain(String customerName) throws SQLException {
        JsonArray domainList = new JsonArray();
        Connection conn = dbConnectionManager.getDataSource().getConnection();
        PreparedStatement pstmt = conn.prepareStatement("select customer_id from customer where customer_name = ?");
        pstmt.setString(1, customerName);
        ResultSet rs = pstmt.executeQuery();
        rs.next();

        String customerId = rs.getString("customer_id");

        pstmt = conn.prepareStatement("select * from registered_domain where customer_id = ?");
        pstmt.setString(1, customerId);
        rs = pstmt.executeQuery();
        boolean virgin = false;
        if(!rs.next()) {
            virgin = true;
        }
        if (virgin) {
            pstmt = conn.prepareStatement("SELECT d.domain_id, d.domain " +
                    "FROM domain d " +
                    "WHERE d.domain_id NOT IN (SELECT rd.domain_id " +
                    "FROM registered_domain rd WHERE rd.customer_id = ?) " +
                    "LIMIT 10");
            pstmt.setString(1, customerId);
            rs = pstmt.executeQuery();
            while(rs.next()){
                JsonObject jo = new JsonObject();
                jo.addProperty("domain", rs.getString("domain"));
                domainList.add(jo);
            }
        } else {
            pstmt = conn.prepareStatement("SELECT d.domain_id, d.domain, COUNT(rd.domain_id) AS usage_count " +
                            "FROM registered_domain rd " +
                            "JOIN domain d ON rd.domain_id = d.domain_id " +
                            "WHERE rd.customer_id = ? " +
                            "GROUP BY d.domain_id, d.domain " +
                            "ORDER BY usage_count ASC " +
                            "LIMIT 10;");
            pstmt.setString(1, customerId);
            rs = pstmt.executeQuery();
            while(rs.next()) {
                JsonObject jo = new JsonObject();
                jo.addProperty("domain", rs.getString("domain"));
                domainList.add(jo);
            }
        }
        return domainList;
    }

    private JsonObject retrieveRegisteredDomainInfo(String customerName) throws Exception {
        JsonObject resposne = new JsonObject();
        Connection conn = dbConnectionManager.getDataSource().getConnection();
        PreparedStatement pstmt = conn.prepareStatement("select * from customer where customer_name = ?;");
        pstmt.setString(1, customerName);
        ResultSet rs = pstmt.executeQuery();
        resposne.addProperty("customer_name", customerName);
        JsonArray requestList = new JsonArray();
        while(rs.next()) {
            JsonObject requestId = new JsonObject();
            String customerId = rs.getString("customer_id");
            requestId.addProperty("customerId", customerId);
            requestId.addProperty("pageUrl",rs.getString("page_url"));
            PreparedStatement pstmtDomainList = conn.prepareStatement("select domain_id, domain, count(*) as domainUsedCnt from registered_domain where customer_id = ? " +
                    "group by domain_id, domain;");
            pstmtDomainList.setString(1, customerId);
            ResultSet rsDomainList = pstmtDomainList.executeQuery();

            JsonArray domainList = new JsonArray();
            while(rsDomainList.next()) {
                JsonObject domainElem = new JsonObject();
                String domainId = rsDomainList.getString("domain_id");
                domainElem.addProperty("domainId", domainId);
                domainElem.addProperty("domain", rsDomainList.getString("domain"));
                domainElem.addProperty("domainUsedCnt", rsDomainList.getInt("domainUsedCnt"));

                PreparedStatement pstmtUrls = conn.prepareStatement("select domain_url, published_at from registered_domain where domain_id = ?;");
                pstmtUrls.setString(1, domainId);
                ResultSet rsUrls = pstmtUrls.executeQuery();

                JsonArray domainUrlList = new JsonArray();
                while(rsUrls.next()) {
                    JsonObject domainUrl = new JsonObject();
                    domainUrl.addProperty("domainUrl", rsUrls.getString("domain_url"));
                    domainUrl.addProperty("publishedAt", rsUrls.getString("published_at"));
                    domainUrlList.add(domainUrl);
                }
                domainElem.add("domainUrlList", domainUrlList);
                domainList.add(domainElem);
            }
            requestId.add("domainList", domainList);
            requestList.add(requestId);
        }
        resposne.add("requestList", requestList);

        return resposne;
    }

    private String retrieveCustomerInfo(String customerName) throws Exception {
        Connection conn = dbConnectionManager.getDataSource().getConnection();
        PreparedStatement pstmt = conn.prepareStatement("select * from customer where customer_name = ?;");
        pstmt.setString(1, customerName);
        ResultSet rs = pstmt.executeQuery();
        if(rs.next()) {
            return rs.getString("customer_id");
        } else {
            return null;
        }
    }
}
