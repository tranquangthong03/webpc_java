package com.example.travelweb.controller;

import com.example.travelweb.entity.Tour;
import com.example.travelweb.service.TourService;
import com.example.travelweb.service.CategoryService;
import com.example.travelweb.service.DestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tours")
public class TourController {
    
    @Autowired
    private TourService tourService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private DestinationService destinationService;
    
    // Danh sách tour với tìm kiếm và filter
    @GetMapping
    public String listTours(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "12") int size,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) Long destinationId,
                           @RequestParam(required = false) String keyword,                           @RequestParam(required = false) BigDecimal minPrice,
                           @RequestParam(required = false) BigDecimal maxPrice,
                           @RequestParam(required = false) Integer durationDays,
                           Model model) {
        
        Page<Tour> tours = tourService.searchToursWithFilters(
            categoryId, destinationId, keyword, minPrice, maxPrice, durationDays, page, size);
          model.addAttribute("tours", tours);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("destinations", destinationService.getActiveDestinations());
        
        // Giữ lại các tham số filter
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentDestinationId", destinationId);
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("currentMinPrice", minPrice);
        model.addAttribute("currentMaxPrice", maxPrice);
        model.addAttribute("currentDurationDays", durationDays);
        
        return "tours/list";
    }
    
    // Chi tiết tour
    @GetMapping("/{id}")
    public String tourDetail(@PathVariable Long id, Model model) {
        System.out.println("DEBUG: Đang truy cập chi tiết tour với ID: " + id);
        Optional<Tour> tourOptional = tourService.getTourById(id);
        
        if (tourOptional.isEmpty()) {
            System.out.println("DEBUG: Không tìm thấy tour với ID: " + id);
            return "redirect:/tours?error=notfound";
        }
        
        Tour tour = tourOptional.get();
        System.out.println("DEBUG: Tìm thấy tour: " + tour.getTourName() + " (ID: " + tour.getTourId() + ")");
        
        // Kiểm tra chi tiết các trường quan trọng
        System.out.println("DEBUG: Tour category: " + (tour.getCategory() != null ? tour.getCategory().getCategoryId() + " - " + tour.getCategory().getCategoryName() : "NULL"));
        System.out.println("DEBUG: Tour destination: " + (tour.getDestination() != null ? tour.getDestination().getDestinationId() + " - " + tour.getDestination().getDestinationName() : "NULL"));
        System.out.println("DEBUG: Tour status: " + tour.getStatus());
        
        // Kiểm tra các trường quan trọng
        if (tour.getCategory() == null) {
            System.out.println("DEBUG: Tour ID " + id + " không có category");
            model.addAttribute("errorMessage", "Tour này thiếu thông tin danh mục");
            return "error/tour-error";
        }
        
        if (tour.getDestination() == null) {
            System.out.println("DEBUG: Tour ID " + id + " không có destination");
            model.addAttribute("errorMessage", "Tour này thiếu thông tin điểm đến");
            return "error/tour-error";
        }
        
        if (tour.getStatus() != Tour.Status.ACTIVE) {
            System.out.println("DEBUG: Tour ID " + id + " không ở trạng thái ACTIVE: " + tour.getStatus());
            model.addAttribute("errorMessage", "Tour này hiện không khả dụng");
            return "error/tour-error";
        }
        
        // Nếu mọi thứ OK, hiển thị chi tiết tour
        System.out.println("DEBUG: Hiển thị chi tiết tour: " + tour.getTourName());
        model.addAttribute("tour", tour);
        
        try {
            // Lấy các tour liên quan (cùng category hoặc destination)
            List<Tour> relatedTours = tourService.getToursByCategory(tour.getCategory().getCategoryId())
                    .stream()
                    .filter(t -> !t.getTourId().equals(id))
                    .limit(4)
                    .toList();
            System.out.println("DEBUG: Tìm thấy " + relatedTours.size() + " tour liên quan");
            model.addAttribute("relatedTours", relatedTours);
        } catch (Exception e) {
            System.out.println("DEBUG: Lỗi khi lấy tour liên quan: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("relatedTours", new ArrayList<>());
        }
        
        return "tours/detail";
    }
    
    // Tour theo category
    @GetMapping("/category/{categoryId}")
    public String toursByCategory(@PathVariable Long categoryId,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size,
                                 Model model) {
        
        return listTours(page, size, categoryId, null, null, null, null, null, model);
    }
    
    // Tour theo destination
    @GetMapping("/destination/{destinationId}")
    public String toursByDestination(@PathVariable Long destinationId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "12") int size,
                                   Model model) {
        
        return listTours(page, size, null, destinationId, null, null, null, null, model);
    }
    
    // Tìm kiếm tour
    @GetMapping("/search")
    public String searchTours(@RequestParam String keyword,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size,
                             Model model) {
        
        return listTours(page, size, null, null, keyword, null, null, null, model);
    }
    
    // Tour nổi bật
    @GetMapping("/featured")
    public String featuredTours(Model model) {
        model.addAttribute("tours", tourService.getFeaturedTours());
        model.addAttribute("title", "Tour Nổi Bật");
        return "tours/featured";
    }
    
    // Tour có giảm giá
    @GetMapping("/discount")
    public String discountTours(Model model) {
        model.addAttribute("tours", tourService.getToursWithDiscount());
        model.addAttribute("title", "Tour Giảm Giá");
        return "tours/discount";
    }
}
