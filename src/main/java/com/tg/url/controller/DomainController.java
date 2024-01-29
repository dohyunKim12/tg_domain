package com.tg.url.controller;

import com.tg.url.config.TgConstants;
import com.tg.url.service.DbConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

@Controller
public class DomainUploadController {

    public static final Logger logger = LogManager.getLogger(DomainUploadController.class);

    private final DbConnectionManager dbConnectionManager;
    private String savedFilePath = null;

    @Autowired
    public DomainUploadController(DbConnectionManager dbConnectionManager) {
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

        upsertDomainInfo(); //insert data in db

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


    private void upsertDomainInfo() {
        try {
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
                String sql = "INSERT INTO domain (domain) VALUES (?) ON DUPLICATE KEY UPDATE domain = domain;";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, domainStr);
                pstmt.execute();
                logger.info("domain string {} inserted", domainStr);
            }

            workbook.close();
            excelFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
