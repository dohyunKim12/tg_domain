package com.tg.url.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
@Service
public class DbConnectionManager {
    private final DataSource dataSource;

    @Autowired // application.yml 로부터 DataSource 자동 주입
    public DbConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}

