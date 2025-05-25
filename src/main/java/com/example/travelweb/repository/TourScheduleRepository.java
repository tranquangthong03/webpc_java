package com.example.travelweb.repository;

import com.example.travelweb.entity.TourSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Long> {
    
    // Tìm schedules theo tour
    List<TourSchedule> findByTourTourIdOrderByDepartureDateAsc(Long tourId);
    
    // Tìm schedules theo tour và status
    List<TourSchedule> findByTourTourIdAndStatusOrderByDepartureDateAsc(Long tourId, TourSchedule.Status status);
    
    // Tìm schedules available (còn chỗ)
    @Query("SELECT s FROM TourSchedule s WHERE s.status = 'AVAILABLE' AND s.availableSlots > s.bookedSlots")
    List<TourSchedule> findAvailableSchedules();
    
    // Tìm schedules available theo tour
    @Query("SELECT s FROM TourSchedule s WHERE s.tour.tourId = :tourId AND s.status = 'AVAILABLE' AND s.availableSlots > s.bookedSlots " +
           "ORDER BY s.departureDate ASC")
    List<TourSchedule> findAvailableSchedulesByTour(@Param("tourId") Long tourId);
    
    // Tìm schedules theo khoảng thời gian departure
    @Query("SELECT s FROM TourSchedule s WHERE s.departureDate BETWEEN :startDate AND :endDate ORDER BY s.departureDate ASC")
    List<TourSchedule> findByDepartureDateBetween(@Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);
    
    // Tìm schedules theo tour và khoảng thời gian
    @Query("SELECT s FROM TourSchedule s WHERE s.tour.tourId = :tourId AND s.departureDate BETWEEN :startDate AND :endDate " +
           "ORDER BY s.departureDate ASC")
    List<TourSchedule> findByTourAndDepartureDateBetween(@Param("tourId") Long tourId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);
    
    // Tìm schedules sắp khởi hành
    @Query("SELECT s FROM TourSchedule s WHERE s.departureDate BETWEEN :today AND :futureDate AND s.status = 'AVAILABLE' " +
           "ORDER BY s.departureDate ASC")
    List<TourSchedule> findUpcomingSchedules(@Param("today") LocalDate today, 
                                           @Param("futureDate") LocalDate futureDate);
    
    // Tìm schedules đã qua (cần cập nhật status)
    @Query("SELECT s FROM TourSchedule s WHERE s.returnDate < :today AND s.status != 'COMPLETED'")
    List<TourSchedule> findPastSchedulesNotCompleted(@Param("today") LocalDate today);
    
    // Tìm schedules theo guide
    List<TourSchedule> findByGuideNameContainingOrderByDepartureDateAsc(String guideName);
    
    // Tìm schedules có availability thấp
    @Query("SELECT s FROM TourSchedule s WHERE (s.availableSlots - s.bookedSlots) <= :threshold AND s.status = 'AVAILABLE' " +
           "ORDER BY (s.availableSlots - s.bookedSlots) ASC")
    List<TourSchedule> findLowAvailabilitySchedules(@Param("threshold") Integer threshold);
    
    // Tìm schedules đầy (full)
    @Query("SELECT s FROM TourSchedule s WHERE s.availableSlots <= s.bookedSlots")
    List<TourSchedule> findFullSchedules();
    
    // Đếm schedules theo tour
    long countByTourTourId(Long tourId);
    
    // Đếm schedules available theo tour
    @Query("SELECT COUNT(s) FROM TourSchedule s WHERE s.tour.tourId = :tourId AND s.status = 'AVAILABLE' AND s.availableSlots > s.bookedSlots")
    long countAvailableSchedulesByTour(@Param("tourId") Long tourId);
    
    // Đếm schedules theo status
    long countByStatus(TourSchedule.Status status);
    
    // Thống kê booking theo schedule
    @Query("SELECT s.scheduleId, s.tour.tourName, s.departureDate, s.bookedSlots, s.availableSlots " +
           "FROM TourSchedule s ORDER BY s.bookedSlots DESC")
    List<Object[]> getScheduleBookingStats();
    
    // Tìm schedules có doanh thu cao nhất
    @Query("SELECT s, SUM(b.finalAmount) as revenue FROM TourSchedule s JOIN s.bookings b " +
           "WHERE b.bookingStatus = 'CONFIRMED' AND b.paymentStatus = 'PAID' " +
           "GROUP BY s ORDER BY revenue DESC")
    List<Object[]> findTopRevenueSchedules(Pageable pageable);
    
    // Tìm schedules theo khoảng giá
    @Query("SELECT s FROM TourSchedule s WHERE s.price BETWEEN :minPrice AND :maxPrice ORDER BY s.price ASC")
    List<TourSchedule> findByPriceBetween(@Param("minPrice") java.math.BigDecimal minPrice, 
                                         @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    // Tìm schedules cần cập nhật status (sắp departure)
    @Query("SELECT s FROM TourSchedule s WHERE s.departureDate = :tomorrow AND s.status = 'AVAILABLE'")
    List<TourSchedule> findSchedulesDepartingTomorrow(@Param("tomorrow") LocalDate tomorrow);
    
    // Tìm schedules theo destination (thông qua tour)
    @Query("SELECT s FROM TourSchedule s WHERE s.tour.destination.destinationId = :destinationId " +
           "ORDER BY s.departureDate ASC")
    List<TourSchedule> findByDestinationId(@Param("destinationId") Long destinationId);
    
    // Tìm schedules theo category (thông qua tour)
    @Query("SELECT s FROM TourSchedule s WHERE s.tour.category.categoryId = :categoryId " +
           "ORDER BY s.departureDate ASC")
    List<TourSchedule> findByCategoryId(@Param("categoryId") Long categoryId);
    
    // Tìm schedules có nhiều booking nhất
    @Query("SELECT s FROM TourSchedule s ORDER BY s.bookedSlots DESC")
    List<TourSchedule> findTopBookedSchedules(Pageable pageable);
    
    // Tìm schedules theo meeting point
    List<TourSchedule> findByMeetingPointContainingOrderByDepartureDateAsc(String meetingPoint);
    
    // Kiểm tra schedule có booking nào không
    @Query("SELECT COUNT(b) > 0 FROM TourSchedule s JOIN s.bookings b WHERE s.scheduleId = :scheduleId")
    boolean hasBookings(@Param("scheduleId") Long scheduleId);
    
    // Tìm schedules cần cancel (không có booking và gần departure date)
    @Query("SELECT s FROM TourSchedule s WHERE s.bookedSlots = 0 AND s.departureDate BETWEEN :today AND :cancelDate " +
           "AND s.status = 'AVAILABLE'")
    List<TourSchedule> findSchedulesNeedingCancellation(@Param("today") LocalDate today, 
                                                       @Param("cancelDate") LocalDate cancelDate);
    
    // Tìm schedules theo tour code
    @Query("SELECT s FROM TourSchedule s WHERE s.tour.tourCode = :tourCode ORDER BY s.departureDate ASC")
    List<TourSchedule> findByTourCode(@Param("tourCode") String tourCode);
    
    // Tổng số slots available của tất cả schedules active
    @Query("SELECT SUM(s.availableSlots - s.bookedSlots) FROM TourSchedule s WHERE s.status = 'AVAILABLE'")
    Long getTotalAvailableSlots();
    
    // Tìm schedules theo duration (thông qua tour)
    @Query("SELECT s FROM TourSchedule s WHERE s.tour.durationDays = :duration ORDER BY s.departureDate ASC")
    List<TourSchedule> findByTourDuration(@Param("duration") Integer duration);
}
