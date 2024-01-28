package com.tg.url.controller;

import com.tg.url.service.DbConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collections;

@Controller
public class DomainUploadController {

    public static final Logger logger = LogManager.getLogger(DomainUploadController.class);

    private final DbConnectionManager dbConnectionManager;

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

        additionalMethod();

        return ResponseEntity.ok(Collections.singletonMap("message", "uploadSuccess"));
    }

    private void saveFile(MultipartFile file) {
        try {
            String uploadDir = "/tmp/uploads";

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String fileName = file.getOriginalFilename();
            String filePath = uploadDir + File.separator + fileName;
            file.transferTo(new File(filePath));
            logger.info("File saved done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void additionalMethod() {
//        String excelFilePath = "path/to/your/excel.xlsx"; // 엑셀 파일 경로
//
//        try {
//            Connection conn = dbConnectionManager.getDataSource().getConnection();
//            FileInputStream excelFile = new FileInputStream(new File(excelFilePath));
//            Workbook workbook = new XSSFWorkbook(excelFile);
//            Sheet sheet = workbook.getSheetAt(0);
//
//            for (Row row : sheet) {
//                String value1 = row.getCell(0).getStringCellValue(); // 첫 번째 열의 값
//                String value2 = row.getCell(1).getStringCellValue(); // 두 번째 열의 값
//
//                // MySQL에 데이터를 저장하는 PreparedStatement 작성
//                String sql = "INSERT INTO your_table (column1, column2) VALUES (?, ?)";
//                PreparedStatement preparedStatement = conn.prepareStatement(sql);
//                preparedStatement.setString(1, value1);
//                preparedStatement.setString(2, value2);
//                preparedStatement.executeUpdate();
//                preparedStatement.close();
//            }
//
//            workbook.close();
//            excelFile.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
