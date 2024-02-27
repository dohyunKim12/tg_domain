package com.tg.url.controller;

import com.tg.url.service.DbConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

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
    public ResponseEntity insertNewPageUrl(@RequestParam("clientUrl") String clientUrl, @RequestParam("domain") String domain, @RequestParam("category") String category, @RequestParam("newPageUrl") String newPageUrl) throws SQLException {
        logger.info("clientUrl : {}, domain : {} ", clientUrl, domain);
        logger.info("category : {}, newPageUrl : {} ", category, newPageUrl);

        Connection conn = dbConnectionManager.getDataSource().getConnection();
//        PreparedStatement pstmt = conn.prepareStatement("select * from client_url where client_url = ?;");
//        pstmt.setString(1, url);
//        ResultSet rs = pstmt.executeQuery();
//        if(rs.isBeforeFirst()) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("message", "Failed to insert new client url"));
//        } else {
//            pstmt = conn.prepareStatement("insert into client_url (client_url, client_name) values (?, ?);");
//            pstmt.setString(1, url);
//            pstmt.setString(2, clientName);
//            pstmt.execute();
//        }
//        conn.commit();
//        conn.close();
        return ResponseEntity.ok(Collections.singletonMap("message", "new page url insert success"));
    }


}
