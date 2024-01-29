package com.tg.url.config;

import java.io.File;

public class TgConstants {
    public static final String USER_DIR =  System.getProperty("user.dir");

    public static final String DEFAULT_UPLOAD_PATH = USER_DIR + "/binary/uploads";
    public static final String DOMAIN_UPLOAD_PATH = DEFAULT_UPLOAD_PATH + "/domains";
    public static final String CUSTOMER_UPLOAD_PATH = DEFAULT_UPLOAD_PATH + "/customers";

    static {
        File directory = new File(DEFAULT_UPLOAD_PATH);
        if(!directory.exists()) {
            directory.mkdirs();
        }
        directory = new File(DOMAIN_UPLOAD_PATH);
        if(!directory.exists()) {
            directory.mkdirs();
        }
        directory = new File(CUSTOMER_UPLOAD_PATH);
        if(!directory.exists()) {
            directory.mkdirs();
        }
    }
}
