package com.tg.url;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Version {
    public static final Logger logger = LogManager.getLogger(Version.class);

    public static void main(String[] args) {
        logger.info("Domain-app started");
        System.out.println(System.getProperty("user.dir"));

        ApplicationContext appContext = SpringApplication.run(Version.class, args);
    }
}
