package com.tg.url.dto;

import lombok.Getter;
import lombok.Setter;

public class PageRegisterRequest {
    @Getter
    @Setter
    private String clientUrl;
    @Getter
    @Setter
    private String domain;
    @Getter
    @Setter
    private String category;
    @Getter
    @Setter
    private String newPageUrl;
}
