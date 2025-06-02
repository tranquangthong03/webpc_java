package com.example.travelweb.repository;

import com.example.travelweb.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Tìm booking theo code
    @Query("SELECT b FROM Booking b JOIN FETCH b.user u WHERE b.bookingCode = :bookingCode")
    Optional<Booking> findByBookingCode(@Param("bookingCode") String bookingCode);
    
    // Tìm booking theo user
    @Query("SELECT b FROM Booking b JOIN FETCH b.user u WHERE b.user.userId = :userId ORDER BY b.bookingDate DESC")
    List<Booking> findByUserUserIdOrderByBookingDateDesc(@Param("userId") Long userId);
    
    // Tìm booking theo user với phân trang
    @Query(value = "SELECT b FROM Booking b JOIN FETCH b.user u WHERE b.user.userId = :userId ORDER BY b.bookingDate DESC",
           countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.user.userId = :userId")
    Page<Booking> findByUserUserIdOrderByBookingDateDesc(@Param("userId") Long userId, Pageable pageable);
    
    // Tìm booking theo status
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.user u " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "WHERE b.bookingStatus = :status " +
           "ORDER BY b.bookingDate DESC")
    List<Booking> findByBookingStatus(@Param("status") Booking.BookingStatus status);
    
    // Tìm booking theo payment status
    @Query("SELECT b FROM Booking b JOIN FETCH b.user u WHERE b.paymentStatus = :paymentStatus")
    List<Booking> findByPaymentStatus(@Param("paymentStatus") Booking.PaymentStatus paymentStatus);
    
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

    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.user u " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "ORDER BY b.bookingDate DESC")
    @Override
    List<Booking> findAll();
    
    // Xóa payments liên quan đến booking
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM Payments WHERE booking_id = :bookingId", nativeQuery = true)
    void deletePaymentsByBookingId(@Param("bookingId") Long bookingId);
}
