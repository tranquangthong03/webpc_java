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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

@Controller
@RequestMapping("/tours")
public class TourController {
    
    private static final Logger logger = LoggerFactory.getLogger(TourController.class);
    
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
        try {
            logger.info("Đang xem chi tiết tour với ID: {}", id);
            Optional<Tour> tourOptional = tourService.getTourById(id);
            
            if (tourOptional.isEmpty()) {
                logger.warn("Không tìm thấy tour với ID: {}", id);
                model.addAttribute("errorMessage", "Tour không tồn tại hoặc đã bị xóa.");
                return "error/tour-error";
            }
            
            Tour tour = tourOptional.get();
            
            // Kiểm tra tour có trạng thái ACTIVE không
            if (tour.getStatus() != Tour.Status.ACTIVE) {
                logger.warn("Tour ID {} không active, status = {}", id, tour.getStatus());
                model.addAttribute("errorMessage", "Tour này hiện không khả dụng.");
                return "error/tour-error";
            }
            
            // Kiểm tra các thuộc tính có thể null và xử lý
            if (tour.getCategory() == null) {
                logger.warn("Tour ID {} không có category", id);
                model.addAttribute("categoryMissing", true);
            }
            
            if (tour.getDestination() == null) {
                logger.warn("Tour ID {} không có destination", id);
                model.addAttribute("destinationMissing", true);
            }
            
            // Đảm bảo itineraries không null để tránh NPE
            if (tour.getItineraries() == null) {
                tour.setItineraries(java.util.Collections.emptyList());
                logger.warn("Tour ID {} có itineraries null, đã thiết lập thành danh sách rỗng", id);
            }
            
            // Kiểm tra và đảm bảo các trường chuỗi không null
            if (tour.getIncludes() == null) tour.setIncludes("");
            if (tour.getExcludes() == null) tour.setExcludes("");
            if (tour.getDescription() == null) tour.setDescription("");
            
            model.addAttribute("tour", tour);
            
            // Lấy các tour liên quan (cùng category hoặc destination)
            try {
                if (tour.getCategory() != null) {
                    model.addAttribute("relatedTours", tourService.getToursByCategory(tour.getCategory().getCategoryId())
                            .stream()
                            .filter(t -> !t.getTourId().equals(id))
                            .filter(t -> t.getStatus() == Tour.Status.ACTIVE)
                            .limit(4)
                            .toList());
                } else if (tour.getDestination() != null) {
                    // Thử lấy tour theo điểm đến nếu không có danh mục
                    model.addAttribute("relatedTours", tourService.getToursByDestination(tour.getDestination().getDestinationId())
                            .stream()
                            .filter(t -> !t.getTourId().equals(id))
                            .filter(t -> t.getStatus() == Tour.Status.ACTIVE)
                            .limit(4)
                            .toList());
                } else {
                    // Nếu không có cả danh mục và điểm đến, hiển thị danh sách rỗng
                    model.addAttribute("relatedTours", java.util.Collections.emptyList());
                }
            } catch (Exception e) {
                logger.warn("Không thể lấy danh sách tour liên quan: {}", e.getMessage());
                model.addAttribute("relatedTours", java.util.Collections.emptyList());
            }
            
            return "tours/detail";
        } catch (Exception e) {
            logger.error("Lỗi khi hiển thị chi tiết tour ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi hiển thị thông tin tour: " + e.getMessage());
            model.addAttribute("errorType", e.getClass().getSimpleName());
            model.addAttribute("requestedTourId", id);
            return "error/tour-error";
        }
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
