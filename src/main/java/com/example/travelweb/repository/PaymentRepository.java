package com.example.travelweb.repository;

import com.example.travelweb.entity.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Tìm payment theo transaction ID
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Tìm payments theo booking
    List<Payment> findByBookingBookingIdOrderByPaymentDateDesc(Long bookingId);
    
    // Tìm payments theo payment method
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
    
    // Tìm payments theo status
    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);
    
    // Tìm payments thành công của booking
    @Query("SELECT p FROM Payment p WHERE p.booking.bookingId = :bookingId AND p.paymentStatus = :status")
    List<Payment> findSuccessfulPaymentsByBooking(@Param("bookingId") Long bookingId, 
                                                 @Param("status") Payment.PaymentStatus status);
    
    // Tìm payments trong khoảng thời gian
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // Tìm payments thành công trong khoảng thời gian
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.paymentStatus = :status")
    List<Payment> findSuccessfulPaymentsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate,
                                                   @Param("status") Payment.PaymentStatus status);
    
    // Overloaded method cho compatibility
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.paymentStatus = com.example.travelweb.entity.Payment$PaymentStatus.SUCCESS")
    List<Payment> findSuccessfulPaymentsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    // Thống kê doanh thu theo phương thức thanh toán
    @Query("SELECT p.paymentMethod, COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status GROUP BY p.paymentMethod")
    List<Object[]> getRevenueByPaymentMethod(@Param("status") Payment.PaymentStatus status);
    
    // Overloaded method cho compatibility
    @Query("SELECT p.paymentMethod, COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = com.example.travelweb.entity.Payment$PaymentStatus.SUCCESS GROUP BY p.paymentMethod")
    List<Object[]> getRevenueByPaymentMethod();
    
    // Thống kê payments theo tháng
    @Query("SELECT YEAR(p.paymentDate), MONTH(p.paymentDate), COUNT(p), COALESCE(SUM(p.amount), 0) " +
           "FROM Payment p WHERE p.paymentStatus = :status " +
           "GROUP BY YEAR(p.paymentDate), MONTH(p.paymentDate) " +
           "ORDER BY YEAR(p.paymentDate) DESC, MONTH(p.paymentDate) DESC")
    List<Object[]> getPaymentStatsByMonth(@Param("status") Payment.PaymentStatus status);
    
    // Đếm payments theo status
    long countByPaymentStatus(Payment.PaymentStatus status);
    
    // Đếm payments theo method
    long countByPaymentMethod(Payment.PaymentMethod method);
    
    // Tổng doanh thu từ payments thành công
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status AND p.amount > 0")
    Double getTotalSuccessfulPayments(@Param("status") Payment.PaymentStatus status);
    
    // Overloaded method cho compatibility
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = com.example.travelweb.entity.Payment$PaymentStatus.SUCCESS AND p.amount > 0")
    Double getTotalSuccessfulPayments();
    
    // Doanh thu theo ngày
    @Query("SELECT DATE(p.paymentDate), COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status AND p.amount > 0 " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(p.paymentDate) ORDER BY DATE(p.paymentDate)")
    List<Object[]> getDailyRevenue(@Param("status") Payment.PaymentStatus status,
                                   @Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    // Overloaded method cho compatibility
    @Query("SELECT DATE(p.paymentDate), COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = com.example.travelweb.entity.Payment$PaymentStatus.SUCCESS AND p.amount > 0 " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(p.paymentDate) ORDER BY DATE(p.paymentDate)")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    // Tìm payments cần xử lý (pending quá lâu)
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = :status AND p.paymentDate < :cutoffDate")
    List<Payment> findPendingPaymentsOlderThan(@Param("status") Payment.PaymentStatus status,
                                              @Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Tìm payments theo booking và status
    List<Payment> findByBookingBookingIdAndPaymentStatus(Long bookingId, Payment.PaymentStatus status);
    
    // Tìm payments theo user (thông qua booking)
    @Query("SELECT p FROM Payment p WHERE p.booking.user.userId = :userId ORDER BY p.paymentDate DESC")
    List<Payment> findByUserId(@Param("userId") Long userId);
    
    // Tìm payments theo tour (thông qua booking và schedule)
    @Query("SELECT p FROM Payment p WHERE p.booking.schedule.tour.tourId = :tourId")
    List<Payment> findByTourId(@Param("tourId") Long tourId);
    
    // Kiểm tra transaction ID đã tồn tại
    boolean existsByTransactionId(String transactionId);
    
    // Tìm top payment methods
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.paymentStatus = :status " +
           "GROUP BY p.paymentMethod ORDER BY COUNT(p) DESC")
    List<Object[]> getTopPaymentMethods(@Param("status") Payment.PaymentStatus status);
    
    // Tìm payments có amount lớn nhất
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = :status ORDER BY p.amount DESC")
    List<Payment> findTopPaymentsByAmount(@Param("status") Payment.PaymentStatus status, Pageable pageable);
    
    // Tính tổng refund
    @Query("SELECT COALESCE(SUM(ABS(p.amount)), 0) FROM Payment p WHERE p.paymentStatus = :status AND p.amount < 0")
    Double getTotalRefundAmount(@Param("status") Payment.PaymentStatus status);
    
    // Tìm payments cần refund (theo booking cancelled)
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = :paymentStatus AND p.booking.bookingStatus = :bookingStatus")
    List<Payment> findPaymentsNeedingRefund(@Param("paymentStatus") Payment.PaymentStatus paymentStatus,
                                           @Param("bookingStatus") com.example.travelweb.entity.Booking.BookingStatus bookingStatus);
    
    // Thống kê payment theo tuần
    @Query("SELECT YEAR(p.paymentDate), WEEK(p.paymentDate), COUNT(p), COALESCE(SUM(p.amount), 0) " +
           "FROM Payment p WHERE p.paymentStatus = :status " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(p.paymentDate), WEEK(p.paymentDate) " +
           "ORDER BY YEAR(p.paymentDate), WEEK(p.paymentDate)")
    List<Object[]> getPaymentStatsByWeek(@Param("status") Payment.PaymentStatus status,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
    
    // Thống kê payment theo ngày
    @Query("SELECT DATE(p.paymentDate), COUNT(p), COALESCE(SUM(p.amount), 0) " +
           "FROM Payment p WHERE p.paymentStatus = :status " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(p.paymentDate) " +
           "ORDER BY DATE(p.paymentDate)")
    List<Object[]> getPaymentStatsByDay(@Param("status") Payment.PaymentStatus status,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}
