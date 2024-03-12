package com.tg.url.controller;

import com.tg.url.dto.PageRegisterRequest;
import com.tg.url.service.DbConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class PageController {

    public static final Logger logger = LogManager.getLogger(PageController.class);
    private final DbConnectionManager dbConnectionManager;

    @Autowired
    public PageController(DbConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }

    @PostMapping("/page-register")
    @ResponseBody
    public ResponseEntity insertNewPageUrl (@RequestParam("clientUrl") String clientUrl,
                                            @RequestParam("category") String category,
                                            @RequestParam(value = "file", required = false) MultipartFile file) throws SQLException, IOException {
        Connection conn = dbConnectionManager.getDataSource().getConnection();
        conn.setAutoCommit(false);
        PreparedStatement pstmt;
        List<String> pageUrlList = new ArrayList<>();
        if (file != null && !file.isEmpty()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String [] urls = line.split("\\s+");
                    for(String url : urls) {
                        if(!url.isEmpty()) {
                            pageUrlList.add(url.trim());
                        }
                    }
                }
            }
        }

        for(String newPageUrl : pageUrlList) {
            String domain = newPageUrl.split("/")[0];
            logger.info("clientUrl : {}, domain : {} ", clientUrl, domain);
            logger.info("category : {}, newPageUrl : {} ", category, newPageUrl);
            pstmt = conn.prepareStatement("select domain_id from domain where domain = ? and category_name = ?");
            pstmt.setString(1, domain);
            pstmt.setString(2, category);
            ResultSet rs = pstmt.executeQuery();
            if(!rs.isBeforeFirst()) throw new SQLException("No DomainID with domain " + domain + ", category " + category);
            rs.next();
            String domainId = rs.getString("domain_id");

            try {
                pstmt = conn.prepareStatement("insert into page_url (client_url, domain_id, page_url) values (?, ?, ?)");
                pstmt.setString(1, clientUrl);
                pstmt.setString(2, domainId);
                pstmt.setString(3, newPageUrl);
                pstmt.execute();
            } catch (SQLIntegrityConstraintViolationException e) {
                e.printStackTrace();
                throw new RuntimeException("Duplicate entry " + newPageUrl + " for key page_url. PageUrl must be unique");
            }
        }

        pstmt = conn.prepareStatement("update client_url set registered = registered + 1 where client_url = ?");
        pstmt.setString(1, clientUrl);
        pstmt.execute();

        conn.commit();
        conn.close();
        return ResponseEntity.ok().body(pageUrlList);
    }
}
