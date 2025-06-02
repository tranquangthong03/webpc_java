package com.example.travelweb.service;

import com.example.travelweb.entity.Tour;
import com.example.travelweb.entity.Category;
import com.example.travelweb.entity.Destination;
import com.example.travelweb.entity.User;
import com.example.travelweb.entity.TourSchedule;
import com.example.travelweb.entity.TourItinerary;
import com.example.travelweb.repository.TourRepository;
import com.example.travelweb.repository.CategoryRepository;
import com.example.travelweb.repository.DestinationRepository;
import com.example.travelweb.repository.TourScheduleRepository;
import com.example.travelweb.repository.TourItineraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@Transactional
public class TourService {
    
    private static final Logger logger = LoggerFactory.getLogger(TourService.class);
    
    @Autowired
    private TourRepository tourRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private DestinationRepository destinationRepository;
    
    @Autowired
    private TourScheduleRepository tourScheduleRepository;
    
    @Autowired
    private TourItineraryRepository tourItineraryRepository;
    
    // Lấy tất cả tours
    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }
    
    // Lấy tất cả tours (alias)
    public List<Tour> findAll() {
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
    
    // Xóa tour schedules
    private void deleteTourSchedules(Tour tour) {
        logger.debug("Menghapus tour schedules untuk tour ID: {}", tour.getTourId());
        if (tour.getSchedules() != null && !tour.getSchedules().isEmpty()) {
            // Kiểm tra từng schedule và xóa nếu không có bookings
            List<TourSchedule> schedulesToRemove = new ArrayList<>();
            
            for (TourSchedule schedule : tour.getSchedules()) {
                logger.debug("Memeriksa schedule ID: {}", schedule.getScheduleId());
                // Kiểm tra xem schedule có bookings không
                if (schedule.getBookings() == null || schedule.getBookings().isEmpty()) {
                    logger.debug("Schedule ID: {} tidak memiliki booking, menghapus", schedule.getScheduleId());
                    schedulesToRemove.add(schedule);
                    tourScheduleRepository.deleteById(schedule.getScheduleId());
                } else {
                    logger.warn("Schedule ID: {} memiliki booking, tidak dapat dihapus", schedule.getScheduleId());
                    throw new RuntimeException("Không thể xóa tour vì lịch trình tour có ID " + 
                        schedule.getScheduleId() + " đã có khách đặt");
                }
            }
            
            // Xóa schedules khỏi danh sách của tour
            tour.getSchedules().removeAll(schedulesToRemove);
            logger.debug("Đã xóa {} schedules", schedulesToRemove.size());
        } else {
            logger.debug("Tour không có schedules, tidak perlu menghapus");
        }
    }

    // Xóa tour
    @Transactional
    public void deleteTour(Long id) {
        logger.info("Xóa tour với ID: {}", id);
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Tour với ID: {} không tìm thấy", id);
                    return new RuntimeException("Không tìm thấy tour với ID: " + id);
                });
        
        try {
            // Kiểm tra xem tour có booking nào không
            boolean hasBookings = false;
            if (tour.getSchedules() != null) {
                hasBookings = tour.getSchedules().stream()
                    .anyMatch(schedule -> schedule.getBookings() != null && !schedule.getBookings().isEmpty());
            }
                
            if (hasBookings) {
                logger.error("Tour ID: {} có booking, không thể xóa", id);
                throw new RuntimeException("Không thể xóa tour vì đã có khách đặt");
            }
            
            try {
                // Xóa tour schedules trước - Cách 1 (Native SQL)
                logger.debug("Xóa tour schedules cho tour ID: {} (Native SQL)", id);
                tourScheduleRepository.deleteAllByTourId(id);
                
                // Xóa tour itineraries - Cách 1 (Native SQL)
                logger.debug("Xóa tour itineraries cho tour ID: {} (Native SQL)", id);
                tourItineraryRepository.deleteAllByTourId(id);
                
            } catch (Exception e) {
                logger.error("Lỗi khi xóa schedules/itineraries với Native SQL: {}", e.getMessage());
                logger.debug("Chuyển sang phương thức xóa thủ công...");
                
                // Xóa tour schedules - Cách 2 (JPA)
                if (tour.getSchedules() != null) {
                    for (TourSchedule schedule : new ArrayList<>(tour.getSchedules())) {
                        logger.debug("Xóa schedule ID: {}", schedule.getScheduleId());
                        tour.getSchedules().remove(schedule);
                        tourScheduleRepository.delete(schedule);
                    }
                }
                
                // Xóa tour itineraries - Cách 2 (JPA)
                if (tour.getItineraries() != null) {
                    for (TourItinerary itinerary : new ArrayList<>(tour.getItineraries())) {
                        logger.debug("Xóa itinerary ID: {}", itinerary.getItineraryId());
                        tour.getItineraries().remove(itinerary);
                        tourItineraryRepository.delete(itinerary);
                    }
                }
            }
            
            // Flush để đảm bảo các thay đổi được commit vào DB
            tourRepository.flush();
            
            // Xóa tour
            logger.debug("Xóa tour ID: {}", id);
            tourRepository.deleteById(id);
            logger.info("Tour ID: {} đã xóa thành công", id);
            
        } catch (Exception e) {
            logger.error("Lỗi khi xóa tour ID: {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Lỗi khi xóa tour: " + e.getMessage(), e);
        }
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
}
