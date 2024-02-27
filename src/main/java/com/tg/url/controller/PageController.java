package com.tg.url.controller;

import com.tg.url.dto.PageRegisterRequest;
import com.tg.url.service.DbConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity insertNewPageUrl(@RequestBody PageRegisterRequest requst) throws SQLException {
        logger.info("clientUrl : {}, domain : {} ", requst.getClientUrl(), requst.getDomain());
        logger.info("category : {}, newPageUrl : {} ", requst.getCategory(), requst.getNewPageUrl());
        if(!requst.getNewPageUrl().contains(requst.getDomain())) throw new RuntimeException("pageurl must contain domain");

        Connection conn = dbConnectionManager.getDataSource().getConnection();
        conn.setAutoCommit(false);
        PreparedStatement pstmt = conn.prepareStatement("select domain_id from domain where domain = ? and category_name = ?;");
        pstmt.setString(1, requst.getDomain());
        pstmt.setString(2, requst.getCategory());
        ResultSet rs = pstmt.executeQuery();
        if(!rs.isBeforeFirst()) throw new SQLException("No DomainID with domain " + requst.getDomain() + ", category " + requst.getCategory());
        rs.next();
        String domainId = rs.getString("domain_id");

        pstmt = conn.prepareStatement("insert into page_url (client_url, domain_id, page_url) values (?, ?, ?);");
        pstmt.setString(1, requst.getClientUrl());
        pstmt.setString(2, domainId);
        pstmt.setString(3, requst.getNewPageUrl());
        pstmt.execute();
        conn.commit();
        conn.close();
        return ResponseEntity.ok(Collections.singletonMap("message", "new page url insert success"));
    }
}
