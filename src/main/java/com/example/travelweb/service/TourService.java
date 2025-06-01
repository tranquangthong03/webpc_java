package com.example.travelweb.service;

import com.example.travelweb.entity.Tour;
import com.example.travelweb.entity.Category;
import com.example.travelweb.entity.Destination;
import com.example.travelweb.entity.User;
import com.example.travelweb.repository.TourRepository;
import com.example.travelweb.repository.CategoryRepository;
import com.example.travelweb.repository.DestinationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TourService {
    
    @Autowired
    private TourRepository tourRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private DestinationRepository destinationRepository;
    
    // Lấy tất cả tours
    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }
    
    // Lấy các tours active
    public List<Tour> getActiveTours() {
        return tourRepository.findByStatus(Tour.Status.ACTIVE);
    }
    
    // Lấy tour theo ID
    public Optional<Tour> getTourById(Long id) {
        return tourRepository.findById(id);
    }
    
    // Lấy tour theo mã tour
    public Optional<Tour> getTourByCode(String tourCode) {
        return tourRepository.findByTourCode(tourCode);
    }
    
    // Tạo tour mới
    public Tour createTour(Tour tour) {
        // Kiểm tra mã tour đã tồn tại
        if (tourRepository.existsByTourCode(tour.getTourCode())) {
            throw new RuntimeException("Mã tour đã tồn tại: " + tour.getTourCode());
        }
        
        // Kiểm tra category và destination tồn tại
        if (tour.getCategory() == null || !categoryRepository.existsById(tour.getCategory().getCategoryId())) {
            throw new RuntimeException("Danh mục không tồn tại");
        }
        
        if (tour.getDestination() == null || !destinationRepository.existsById(tour.getDestination().getDestinationId())) {
            throw new RuntimeException("Điểm đến không tồn tại");
        }
        
        return tourRepository.save(tour);
    }
    
    // Cập nhật tour
    public Tour updateTour(Long id, Tour tourDetails) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour với ID: " + id));
        
        // Kiểm tra mã tour (nếu thay đổi)
        if (!tour.getTourCode().equals(tourDetails.getTourCode()) && 
            tourRepository.existsByTourCode(tourDetails.getTourCode())) {
            throw new RuntimeException("Mã tour đã tồn tại: " + tourDetails.getTourCode());
        }
        
        // Cập nhật thông tin
        tour.setTourName(tourDetails.getTourName());
        tour.setTourCode(tourDetails.getTourCode());
        tour.setCategory(tourDetails.getCategory());
        tour.setDestination(tourDetails.getDestination());
        tour.setDescription(tourDetails.getDescription());
        tour.setDurationDays(tourDetails.getDurationDays());
        tour.setMaxParticipants(tourDetails.getMaxParticipants());
        tour.setPrice(tourDetails.getPrice());
        tour.setDiscountPercentage(tourDetails.getDiscountPercentage());
        tour.setMainImageUrl(tourDetails.getMainImageUrl());
        tour.setDifficultyLevel(tourDetails.getDifficultyLevel());
        tour.setIncludes(tourDetails.getIncludes());
        tour.setExcludes(tourDetails.getExcludes());
        tour.setStatus(tourDetails.getStatus());
        tour.setFeatured(tourDetails.getFeatured());
        
        return tourRepository.save(tour);
    }
    
    // Thay đổi status tour
    public Tour changeTourStatus(Long id, Tour.Status status) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour với ID: " + id));
        
        tour.setStatus(status);
        return tourRepository.save(tour);
    }
    
    // Thay đổi featured tour
    public Tour changeFeaturedStatus(Long id, Boolean featured) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour với ID: " + id));
        
        tour.setFeatured(featured);
        return tourRepository.save(tour);
    }
    
    // Xóa tour
    public void deleteTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour với ID: " + id));
        
        // Kiểm tra xem tour có booking nào không
        if (!tour.getSchedules().isEmpty()) {
            // Kiểm tra xem có booking nào đang active không
            boolean hasActiveBookings = tour.getSchedules().stream()
                    .anyMatch(schedule -> !schedule.getBookings().isEmpty());
            
            if (hasActiveBookings) {
                throw new RuntimeException("Không thể xóa tour vì đã có khách đặt");
            }
        }
        
        tourRepository.deleteById(id);
    }
      // Lấy tours nổi bật
    public List<Tour> getFeaturedTours() {
        return tourRepository.findByFeaturedTrueAndStatus(Tour.Status.ACTIVE);
    }
    
    // Lấy top tours nổi bật (có giới hạn)
    public List<Tour> getTopFeaturedTours(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return tourRepository.findByFeaturedTrueAndStatusOrderByCreatedAtDesc(Tour.Status.ACTIVE, pageable);
    }
    
    // Lấy tours mới nhất
    public List<Tour> getLatestTours(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return tourRepository.findByStatusOrderByCreatedAtDesc(Tour.Status.ACTIVE, pageable);
    }
    
    // Lấy tours theo category
    public List<Tour> getToursByCategory(Long categoryId) {
        return tourRepository.findByCategoryCategoryIdAndStatus(categoryId, Tour.Status.ACTIVE);
    }
    
    // Lấy tours theo destination
    public List<Tour> getToursByDestination(Long destinationId) {
        return tourRepository.findByDestinationDestinationIdAndStatus(destinationId, Tour.Status.ACTIVE);
    }
    
    // Tìm kiếm tours theo tên
    public List<Tour> searchToursByName(String name) {
        return tourRepository.findByTourNameContainingAndStatus(name, Tour.Status.ACTIVE);
    }
    
    // Lấy tours theo khoảng giá
    public List<Tour> getToursByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return tourRepository.findByPriceBetweenAndStatus(minPrice, maxPrice, Tour.Status.ACTIVE);
    }
    
    // Lấy tours theo số ngày
    public List<Tour> getToursByDuration(Integer days) {
        return tourRepository.findByDurationDaysAndStatus(days, Tour.Status.ACTIVE);
    }
    
    // Lấy tours theo độ khó
    public List<Tour> getToursByDifficulty(Tour.DifficultyLevel level) {
        return tourRepository.findByDifficultyLevelAndStatus(level, Tour.Status.ACTIVE);
    }
      // Lấy tours có giảm giá
    public List<Tour> getToursWithDiscount() {
        return tourRepository.findToursWithDiscount();
    }
      // Tìm kiếm tours với filters và phân trang
    public Page<Tour> searchToursWithFilters(Long categoryId, Long destinationId, String keyword,
                                           BigDecimal minPrice, BigDecimal maxPrice,
                                           Integer durationDays,
                                           int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tourRepository.findToursWithFilters(categoryId, destinationId, 
                minPrice, maxPrice, durationDays, keyword, pageable);
    }
    
    // Đếm số tour theo category
    public long countToursByCategory(Long categoryId) {
        return tourRepository.countByCategoryCategoryIdAndStatus(categoryId, Tour.Status.ACTIVE);
    }
    
    // Đếm số tour theo destination
    public long countToursByDestination(Long destinationId) {
        return tourRepository.countByDestinationDestinationIdAndStatus(destinationId, Tour.Status.ACTIVE);
    }
    
    // Kiểm tra mã tour có tồn tại
    public boolean existsByTourCode(String tourCode) {
        return tourRepository.existsByTourCode(tourCode);
    }
    
    // Lấy top tours được đặt nhiều nhất
    public List<Tour> getTopTours() {
        return tourRepository.findTopBookedTours(PageRequest.of(0, 10));
    }
}
