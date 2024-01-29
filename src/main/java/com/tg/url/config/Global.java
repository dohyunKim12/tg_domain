package com.tg.url.config;

import com.tg.url.controller.DomainUploadController;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

public class Global {
    private static class Holder {
        public static final Global instance = new Global();
    }
    public static Global getInstance () {
        return Holder.instance;
    }
    public static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Global.class);
}
