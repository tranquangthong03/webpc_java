package com.example.travelweb.service;

import com.example.travelweb.entity.Tour;
import com.example.travelweb.entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageService {

    /**
     * Tạo đường dẫn ảnh cho tour dựa theo tour ID
     * 
     * @param tour Tour cần xử lý
     * @return Tour đã được cập nhật đường dẫn ảnh
     */
    public Tour setTourImageIfNotExists(Tour tour) {
        if (tour.getMainImageUrl() == null || tour.getMainImageUrl().isEmpty()) {
            // Sử dụng ID để tạo tên file ảnh định dạng tour-001.jpg
            String formattedId = String.format("%03d", tour.getTourId());
            tour.setMainImageUrl("/images/tours/tour-" + formattedId + ".jpg");
        }
        return tour;
    }

    /**
     * Tạo đường dẫn ảnh cho danh mục dựa theo category ID
     * 
     * @param category Danh mục cần xử lý
     * @return Category đã được cập nhật đường dẫn ảnh
     */
    public Category setCategoryIconIfNotExists(Category category) {
        if (category.getIconUrl() == null || category.getIconUrl().isEmpty()) {
            // Sử dụng ID để tạo tên file icon định dạng category-001.png
            String formattedId = String.format("%03d", category.getCategoryId());
            category.setIconUrl("/images/categories/category-" + formattedId + ".png");
        }
        return category;
    }

    /**
     * Xử lý một danh sách các tour để đảm bảo tất cả đều có đường dẫn ảnh
     * 
     * @param tours Danh sách tour cần xử lý
     * @return Danh sách tour đã được cập nhật đường dẫn ảnh
     */
    public List<Tour> processTourImages(List<Tour> tours) {
        if (tours == null) return null;
        
        tours.forEach(this::setTourImageIfNotExists);
        return tours;
    }

    /**
     * Xử lý một danh sách các danh mục để đảm bảo tất cả đều có đường dẫn icon
     * 
     * @param categories Danh sách danh mục cần xử lý
     * @return Danh sách danh mục đã được cập nhật đường dẫn icon
     */
    public List<Category> processCategoryIcons(List<Category> categories) {
        if (categories == null) return null;
        
        categories.forEach(this::setCategoryIconIfNotExists);
        return categories;
    }
} 