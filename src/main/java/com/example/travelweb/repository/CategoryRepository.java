package com.example.travelweb.repository;

import com.example.travelweb.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Tìm category theo tên
    Optional<Category> findByCategoryName(String categoryName);
    
    // Tìm các category active
    List<Category> findByStatusTrue();
    
    // Tìm category theo tên (tìm kiếm gần đúng)
    @Query("SELECT c FROM Category c WHERE c.categoryName LIKE %:name% AND c.status = true")
    List<Category> findByCategoryNameContainingAndStatusTrue(@Param("name") String name);
    
    // Kiểm tra tên category đã tồn tại
    boolean existsByCategoryName(String categoryName);
    
    // Đếm số tour trong category
    @Query("SELECT c.categoryId, COUNT(t) FROM Category c LEFT JOIN c.tours t WHERE c.status = true GROUP BY c.categoryId")
    List<Object[]> countToursInCategories();
    
    // Tìm các category có tour
    @Query("SELECT DISTINCT c FROM Category c JOIN c.tours t WHERE c.status = true AND t.status = 'ACTIVE'")
    List<Category> findCategoriesWithActiveTours();
}
