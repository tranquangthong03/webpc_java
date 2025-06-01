package com.example.travelweb.controller;

import com.example.travelweb.service.TourService;
import com.example.travelweb.service.CategoryService;
import com.example.travelweb.service.DestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @Autowired
    private TourService tourService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private DestinationService destinationService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Chào mừng đến với Travel Web");
        model.addAttribute("message", "Website du lịch của bạn đang được phát triển!");
        
        // Lấy dữ liệu cho trang chủ
        try {
            model.addAttribute("featuredTours", tourService.getTopFeaturedTours(6));
            model.addAttribute("latestTours", tourService.getLatestTours(8));
            model.addAttribute("categories", categoryService.getCategoriesWithTours());
            model.addAttribute("destinations", destinationService.getDestinationsWithTours());
        } catch (Exception e) {
            // Xử lý lỗi khi database chưa có dữ liệu
            model.addAttribute("featuredTours", java.util.Collections.emptyList());
            model.addAttribute("latestTours", java.util.Collections.emptyList());
            model.addAttribute("categories", java.util.Collections.emptyList());
            model.addAttribute("destinations", java.util.Collections.emptyList());
        }
        
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "Về chúng tôi");
        return "about";
    }
    
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Liên hệ");
        return "contact";
    }
    
    // Redirect mappings for old paths
    @GetMapping("/login")
    public String redirectToLogin() {
        return "redirect:/auth/login";
    }
    
    @GetMapping("/register")
    public String redirectToRegister() {
        return "redirect:/auth/register";
    }
    
    @GetMapping("/test-security")
    public String testSecurity() {
        return "test-security";
    }
}
