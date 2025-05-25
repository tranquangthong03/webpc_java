package com.example.travelweb.repository;

import com.example.travelweb.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Tìm booking theo code
    Optional<Booking> findByBookingCode(String bookingCode);
    
    // Tìm booking theo user
    List<Booking> findByUserUserIdOrderByBookingDateDesc(Long userId);
    
    // Tìm booking theo user với phân trang
    Page<Booking> findByUserUserIdOrderByBookingDateDesc(Long userId, Pageable pageable);
    
    // Tìm booking theo status
    List<Booking> findByBookingStatus(Booking.BookingStatus status);
    
    // Tìm booking theo payment status
    List<Booking> findByPaymentStatus(Booking.PaymentStatus paymentStatus);
    
    // Tìm booking theo schedule
    List<Booking> findByScheduleScheduleId(Long scheduleId);
    
    // Tìm booking theo user và status
    List<Booking> findByUserUserIdAndBookingStatus(Long userId, Booking.BookingStatus status);
    
    // Tìm booking trong khoảng thời gian
    @Query("SELECT b FROM Booking b WHERE b.bookingDate BETWEEN :startDate AND :endDate")
    List<Booking> findByBookingDateBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    // Tìm booking theo tour
    @Query("SELECT b FROM Booking b WHERE b.schedule.tour.tourId = :tourId")
    List<Booking> findByTourId(@Param("tourId") Long tourId);
    
    // Thống kê booking theo tháng
    @Query("SELECT YEAR(b.bookingDate), MONTH(b.bookingDate), COUNT(b) " +
           "FROM Booking b WHERE b.bookingStatus = 'CONFIRMED' " +
           "GROUP BY YEAR(b.bookingDate), MONTH(b.bookingDate) " +
           "ORDER BY YEAR(b.bookingDate) DESC, MONTH(b.bookingDate) DESC")
    List<Object[]> getBookingStatsByMonth();
    
    // Tìm booking cần xác nhận (pending quá lâu)
    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'PENDING' AND b.bookingDate < :cutoffDate")
    List<Booking> findPendingBookingsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Đếm booking theo status
    long countByBookingStatus(Booking.BookingStatus status);
    
    // Đếm booking của user
    long countByUserUserId(Long userId);
    
    // Tổng doanh thu từ booking confirmed
    @Query("SELECT SUM(b.finalAmount) FROM Booking b WHERE b.bookingStatus = 'CONFIRMED' AND b.paymentStatus = 'PAID'")
    Double getTotalRevenue();
    
    // Doanh thu theo tháng
    @Query("SELECT YEAR(b.bookingDate), MONTH(b.bookingDate), SUM(b.finalAmount) " +
           "FROM Booking b WHERE b.bookingStatus = 'CONFIRMED' AND b.paymentStatus = 'PAID' " +
           "GROUP BY YEAR(b.bookingDate), MONTH(b.bookingDate) " +
           "ORDER BY YEAR(b.bookingDate) DESC, MONTH(b.bookingDate) DESC")
    List<Object[]> getRevenueByMonth();
    
    // Kiểm tra booking code đã tồn tại
    boolean existsByBookingCode(String bookingCode);
}
