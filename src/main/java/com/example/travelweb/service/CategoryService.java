package com.example.travelweb.service;

import com.example.travelweb.entity.Category;
import com.example.travelweb.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // Lấy tất cả categories
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    // Lấy các categories active
    public List<Category> getActiveCategories() {
        return categoryRepository.findByStatusTrue();
    }
    
    // Lấy category theo ID
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }
    
    // Lấy category theo tên
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByCategoryName(name);
    }
    
    // Tạo category mới
    public Category createCategory(Category category) {
        // Kiểm tra tên category đã tồn tại
        if (categoryRepository.existsByCategoryName(category.getCategoryName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại: " + category.getCategoryName());
        }
        
        return categoryRepository.save(category);
    }
    
    // Cập nhật category
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        
        // Kiểm tra tên category (nếu thay đổi)
        if (!category.getCategoryName().equals(categoryDetails.getCategoryName()) && 
            categoryRepository.existsByCategoryName(categoryDetails.getCategoryName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại: " + categoryDetails.getCategoryName());
        }
        
        // Cập nhật thông tin
        category.setCategoryName(categoryDetails.getCategoryName());
        category.setDescription(categoryDetails.getDescription());
        category.setIconUrl(categoryDetails.getIconUrl());
        category.setStatus(categoryDetails.getStatus());
        
        return categoryRepository.save(category);
    }
    
    // Thay đổi status category
    public Category changeCategoryStatus(Long id, Boolean status) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        
        category.setStatus(status);
        return categoryRepository.save(category);
    }
    
    // Xóa category
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        
        // Kiểm tra xem category có tour nào không
        if (!category.getTours().isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục vì còn tour thuộc danh mục này");
        }
        
        categoryRepository.deleteById(id);
    }
    
    // Tìm kiếm category theo tên
    public List<Category> searchCategoriesByName(String name) {
        return categoryRepository.findByCategoryNameContainingAndStatusTrue(name);
    }
    
    // Lấy các categories có tour
    public List<Category> getCategoriesWithTours() {
        return categoryRepository.findCategoriesWithActiveTours();
    }
    
    // Kiểm tra tên category có tồn tại
    public boolean existsByName(String name) {
        return categoryRepository.existsByCategoryName(name);
    }
}
