package com.example.travelweb.repository;

import com.example.travelweb.entity.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    
    // Tìm tour theo code
    Optional<Tour> findByTourCode(String tourCode);
    
    // Tìm tour active
    List<Tour> findByStatus(Tour.Status status);
    
    // Tìm tour featured
    List<Tour> findByFeaturedTrueAndStatus(Tour.Status status);
    
    // Tìm tour featured với phân trang
    List<Tour> findByFeaturedTrueAndStatusOrderByCreatedAtDesc(Tour.Status status, Pageable pageable);
    
    // Tìm tour mới nhất
    List<Tour> findByStatusOrderByCreatedAtDesc(Tour.Status status, Pageable pageable);
    
    // Tìm tour theo category
    List<Tour> findByCategoryCategoryIdAndStatus(Long categoryId, Tour.Status status);
    
    // Tìm tour theo destination
    List<Tour> findByDestinationDestinationIdAndStatus(Long destinationId, Tour.Status status);
    
    // Tìm tour theo tên (tìm kiếm gần đúng)
    @Query("SELECT t FROM Tour t WHERE t.tourName LIKE %:name% AND t.status = :status")
    List<Tour> findByTourNameContainingAndStatus(@Param("name") String name, @Param("status") Tour.Status status);
    
    // Tìm tour theo khoảng giá
    @Query("SELECT t FROM Tour t WHERE t.price BETWEEN :minPrice AND :maxPrice AND t.status = :status")
    List<Tour> findByPriceBetweenAndStatus(@Param("minPrice") BigDecimal minPrice, 
                                          @Param("maxPrice") BigDecimal maxPrice, 
                                          @Param("status") Tour.Status status);
    
    // Tìm tour theo thời gian
    List<Tour> findByDurationDaysAndStatus(Integer durationDays, Tour.Status status);
    
    // Tìm tour theo difficulty level
    List<Tour> findByDifficultyLevelAndStatus(Tour.DifficultyLevel difficultyLevel, Tour.Status status);
    
    // Tìm kiếm tour với nhiều điều kiện (phân trang)
    @Query("SELECT t FROM Tour t WHERE " +
           "(:categoryId IS NULL OR t.category.categoryId = :categoryId) AND " +
           "(:destinationId IS NULL OR t.destination.destinationId = :destinationId) AND " +
           "(:minPrice IS NULL OR t.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR t.price <= :maxPrice) AND " +
           "(:durationDays IS NULL OR t.durationDays = :durationDays) AND " +
           "(:keyword IS NULL OR t.tourName LIKE %:keyword% OR t.description LIKE %:keyword%) AND " +
           "t.status = 'ACTIVE'")
    Page<Tour> findToursWithFilters(@Param("categoryId") Long categoryId,
                                   @Param("destinationId") Long destinationId,
                                   @Param("minPrice") BigDecimal minPrice,
                                   @Param("maxPrice") BigDecimal maxPrice,
                                   @Param("durationDays") Integer durationDays,
                                   @Param("keyword") String keyword,
                                   Pageable pageable);
    
    // Top tour có nhiều booking nhất
    @Query("SELECT t FROM Tour t JOIN t.schedules s JOIN s.bookings b " +
           "WHERE t.status = 'ACTIVE' AND b.bookingStatus = 'CONFIRMED' " +
           "GROUP BY t ORDER BY COUNT(b) DESC")
    List<Tour> findTopBookedTours(Pageable pageable);
      // Tour có giảm giá
    @Query("SELECT t FROM Tour t WHERE t.discountPercentage > 0 AND t.status = 'ACTIVE'")
    List<Tour> findToursWithDiscount();
    
    // Kiểm tra tour code đã tồn tại
    boolean existsByTourCode(String tourCode);
    
    // Đếm tour theo category
    long countByCategoryCategoryIdAndStatus(Long categoryId, Tour.Status status);
    
    // Đếm tour theo destination
    long countByDestinationDestinationIdAndStatus(Long destinationId, Tour.Status status);
}
