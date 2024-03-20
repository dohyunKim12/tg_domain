package com.tg.url.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tg.url.config.TgConstants;
import com.tg.url.dto.DomainRecommend;
import com.tg.url.service.DbConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
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
        logger.info("Domain upload end");
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
        conn.setAutoCommit(false);
        FileInputStream excelFile = new FileInputStream(savedFilePath);
        Workbook workbook = new XSSFWorkbook(excelFile);
        Sheet sheet = workbook.getSheetAt(0);

        Map<String, List<String>> domainMap = new HashMap<>();
        int categoriesCnt = sheet.getRow(0).getLastCellNum();
        for(int colIdx = 1; colIdx < categoriesCnt; colIdx++) {
            String category = null;
            List<String> domainSet = new ArrayList<>();
            for(int rowIdx = 0; rowIdx < sheet.getLastRowNum() + 2; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if(row == null) {
                    domainMap.put(category, domainSet);
                    break;
                }
                if(rowIdx == 0) {
                    System.out.println("row: " + rowIdx);
                    System.out.println("col: " + colIdx);
                    category = row.getCell(colIdx).getStringCellValue();
                } else {
                    Cell domainCell = row.getCell(colIdx);
                    if(domainCell != null && !domainCell.equals("")) {
                        String rawDomain = domainCell.getStringCellValue();
                        logger.info("raw domain string: {}", rawDomain);
                        String formedDomain = rawDomain.replaceAll("http://", "")
                                .replaceAll("https://", "")
                                .replaceAll("www.", "");
                        logger.info("formed domain string: {}", formedDomain.replace("\u200B", "") + "...");
                        if(!domainSet.contains(formedDomain)) {
                            domainSet.add(formedDomain.replace("\u200B", ""));
                        }
                    } else {
                        domainMap.put(category, domainSet);
                        break;
                    }
                }
            }
        }

        for(String category : domainMap.keySet()) {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO category (category_name) SELECT ? WHERE NOT EXISTS (SELECT category_name FROM category WHERE category_name = ?)");
            pstmt.setString(1, category);
            pstmt.setString(2, category);
            pstmt.execute();
        }

        for(Map.Entry<String, List<String>> entry : domainMap.entrySet()) {
            String categoryName = entry.getKey();
            List<String> domainList = entry.getValue();
            for(String domain : domainList) {
                String sql = "INSERT INTO domain (category_name, domain) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE domain_id = domain_id";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, categoryName);
                pstmt.setString(2, domain);
                pstmt.execute();
                logger.info("domain string {} inserted", domain);
            }
        }
        workbook.close();
        excelFile.close();
        conn.commit();
        conn.close();
        return true;
    }

    @DeleteMapping("/domain_truncate")
    @ResponseBody
    public ResponseEntity<?> domainTruncate() {
        try {
            Connection conn = dbConnectionManager.getDataSource().getConnection();
            conn.setAutoCommit(false);

            PreparedStatement pstmt = conn.prepareStatement("TRUNCATE TABLE domain");
            pstmt.execute();
            conn.commit();
            conn.close();
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
    @PostMapping("/client-retrieve")
    @ResponseBody
    public ResponseEntity<String> handleClientRetrieve(@RequestParam("client-name") String clientName) {
        // 파일 저장
        logger.info("Client retrieve svc called");

        try {
            retrieveOrInsertClientInfo(clientName);
            // select client_url or insert new client_url
            JsonArray ja = retrieveClientUrl(clientName);
            JsonObject jo = new JsonObject();
            jo.add("clientUrls", ja);

            Gson gson = new Gson();
            return ResponseEntity.ok(gson.toJson(jo));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return ResponseEntity.ok("fail");
        }
    }

    @PostMapping("/new-client-url")
    @ResponseBody
    public ResponseEntity insertNewClientUrl(@RequestParam("new-client-url") String url, @RequestParam("client-name") String clientName) throws SQLException {
        Connection conn = dbConnectionManager.getDataSource().getConnection();
        conn.setAutoCommit(false);
        PreparedStatement pstmt = conn.prepareStatement("select * from client_url where client_url = ?;");
        pstmt.setString(1, url);
        ResultSet rs = pstmt.executeQuery();
        if(rs.isBeforeFirst()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Failed to insert new client url"));
        } else {
            pstmt = conn.prepareStatement("insert into client_url (client_url, client_name) values (?, ?);");
            pstmt.setString(1, url);
            pstmt.setString(2, clientName);
            pstmt.execute();
        }
        conn.commit();
        conn.close();
        return ResponseEntity.ok(Collections.singletonMap("message", "new url insert success"));
    }

    private JsonArray retrieveClientUrl(String clientName) throws SQLException {
        JsonArray result = new JsonArray();
        Connection conn = dbConnectionManager.getDataSource().getConnection();
        conn.setAutoCommit(false);
        PreparedStatement pstmt = conn.prepareStatement("select client_url, registered from client_url where client_name = ?");
        pstmt.setString(1, clientName);
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()) {
            JsonObject jo = new JsonObject();
            jo.addProperty("clientUrl", rs.getString("client_url"));
            jo.addProperty("registered", rs.getString("registered"));
            result.add(jo);
        }
        conn.commit();
        conn.close();
        return result;
    }

    @GetMapping("/domain-category-retrieve")
    @ResponseBody
    public ResponseEntity<String> retrieveCategories(@RequestParam String clientUrl) throws SQLException {
        // 파일 저장
        logger.info("Retrieve category svc called");

        Connection conn = dbConnectionManager.getDataSource().getConnection();
        conn.setAutoCommit(false);
        PreparedStatement pstmt = conn.prepareStatement("select * from category;");
        ResultSet rs = pstmt.executeQuery();
        JsonObject response = new JsonObject();
        JsonObject jo = new JsonObject();
        while(rs.next()) {
            String categoryName = rs.getString("category_name");
            pstmt = conn.prepareStatement("select count(*) from page_url where client_url = ? and category_name = ?");
            pstmt.setString(1, clientUrl);
            pstmt.setString(2, categoryName);
            ResultSet rs2 = pstmt.executeQuery();
            int cntPerCategory = 0;
            if(rs2.next()) {
                cntPerCategory = rs2.getInt(1);
            }
            jo.addProperty(categoryName, cntPerCategory);
        }
        response.add("categories", jo);
        Gson gson = new Gson();
        conn.commit();
        conn.close();
        return ResponseEntity.ok(gson.toJson(response));
    }

    @PostMapping("/domain-recommend")
    @ResponseBody
    public ResponseEntity<String> getReocmmendedDomainList(@RequestBody DomainRecommend request) {
        logger.info("Domain recommend retrieve svc called");
        String clientUrl = request.getUrl();
        String category = request.getCategoryName();
        int requestCnt = request.getRequestCnt();
        try {
            JsonArray ja = getDomain(clientUrl, category, requestCnt);
            Gson gson = new Gson();
            String response = gson.toJson(ja);
            logger.info(response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
            return ResponseEntity.ok("fail");
        }
    }

    private JsonArray getDomain(String clientUrl, String category, int requestCnt) throws SQLException {
        JsonArray result = new JsonArray();
        Connection conn = dbConnectionManager.getDataSource().getConnection();
        conn.setAutoCommit(false);
        PreparedStatement pstmt = conn.prepareStatement("select * from domain where category_name = ?;");
        pstmt.setString(1, category);

        pstmt = conn.prepareStatement("SELECT d.domain_id, d.domain, " +
                "COALESCE(SUM(CASE WHEN p.client_url = ? THEN 1 ELSE 0 END), 0) AS page_url_count " +
                "FROM domain d " +
                "LEFT JOIN page_url p ON d.domain_id = p.domain_id AND p.client_url = ? " +
                "WHERE d.category_name = ? " +
                "GROUP BY d.domain_id, d.domain " +
                "ORDER BY page_url_count ASC, d.domain_id ASC LIMIT ?;");
        pstmt.setString(1, clientUrl);
        pstmt.setString(2, clientUrl);
        pstmt.setString(3, category);
        pstmt.setInt(4, requestCnt);
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()) {
            JsonObject jo = new JsonObject();
            jo.addProperty("recommendedDomain", rs.getString("domain"));
            jo.addProperty("used", rs.getInt(3));
            result.add(jo);
        }
        conn.commit();
        conn.close();
        return result;
    }

    private void retrieveOrInsertClientInfo(String clientName) throws Exception {
        Connection conn = dbConnectionManager.getDataSource().getConnection();
        conn.setAutoCommit(false);
        PreparedStatement pstmt = conn.prepareStatement("select * from client where client_name = ?;");
        pstmt.setString(1, clientName);
        ResultSet rs = pstmt.executeQuery();
        if(!rs.next()) {
            pstmt = conn.prepareStatement("insert into client (client_name) values (?);");
            pstmt.setString(1, clientName);
            pstmt.execute();
        }
        conn.commit();
        conn.close();
    }
}
