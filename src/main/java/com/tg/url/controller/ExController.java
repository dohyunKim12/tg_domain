package com.tg.url.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ExController {

    @GetMapping("/greeting")
    public String greeting(Model model) {
        model.addAttribute("name", "태겸");
        return "greeting";  // Thymeleaf 템플릿을 가리키는 뷰 이름
    }
}

