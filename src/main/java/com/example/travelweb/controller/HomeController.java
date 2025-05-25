package com.example.travelweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Chào mừng đến với Travel Web");
        model.addAttribute("message", "Website du lịch của bạn đang được phát triển!");
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "Về chúng tôi");
        return "about";
    }
}
