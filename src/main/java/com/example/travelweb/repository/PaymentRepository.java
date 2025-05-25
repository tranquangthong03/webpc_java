package com.example.travelweb.repository;

import com.example.travelweb.entity.Payment;
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
    @Query("SELECT p FROM Payment p WHERE p.booking.bookingId = :bookingId AND p.paymentStatus = 'SUCCESS'")
    List<Payment> findSuccessfulPaymentsByBooking(@Param("bookingId") Long bookingId);
    
    // Tìm payments trong khoảng thời gian
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findPaymentsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // Tìm payments thành công trong khoảng thời gian
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate AND p.paymentStatus = 'SUCCESS'")
    List<Payment> findSuccessfulPaymentsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    // Thống kê doanh thu theo phương thức thanh toán
    @Query("SELECT p.paymentMethod, SUM(p.amount) FROM Payment p WHERE p.paymentStatus = 'SUCCESS' GROUP BY p.paymentMethod")
    List<Object[]> getRevenueByPaymentMethod();
    
    // Thống kê payments theo tháng
    @Query("SELECT YEAR(p.paymentDate), MONTH(p.paymentDate), COUNT(p), SUM(p.amount) " +
           "FROM Payment p WHERE p.paymentStatus = 'SUCCESS' " +
           "GROUP BY YEAR(p.paymentDate), MONTH(p.paymentDate) " +
           "ORDER BY YEAR(p.paymentDate) DESC, MONTH(p.paymentDate) DESC")
    List<Object[]> getPaymentStatsByMonth();
    
    // Đếm payments theo status
    long countByPaymentStatus(Payment.PaymentStatus status);
    
    // Đếm payments theo method
    long countByPaymentMethod(Payment.PaymentMethod method);
    
    // Tổng doanh thu từ payments thành công
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentStatus = 'SUCCESS' AND p.amount > 0")
    Double getTotalSuccessfulPayments();
    
    // Doanh thu theo ngày
    @Query("SELECT DATE(p.paymentDate), SUM(p.amount) FROM Payment p WHERE p.paymentStatus = 'SUCCESS' AND p.amount > 0 " +
           "AND p.paymentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(p.paymentDate) ORDER BY DATE(p.paymentDate)")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    // Tìm payments cần xử lý (pending quá lâu)
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PENDING' AND p.paymentDate < :cutoffDate")
    List<Payment> findPendingPaymentsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
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
    @Query("SELECT p.paymentMethod, COUNT(p) FROM Payment p WHERE p.paymentStatus = 'SUCCESS' " +
           "GROUP BY p.paymentMethod ORDER BY COUNT(p) DESC")
    List<Object[]> getTopPaymentMethods();
    
    // Tìm payments có amount lớn nhất
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'SUCCESS' ORDER BY p.amount DESC")
    List<Payment> findTopPaymentsByAmount();
    
    // Tính tổng refund
    @Query("SELECT SUM(ABS(p.amount)) FROM Payment p WHERE p.paymentStatus = 'REFUNDED' AND p.amount < 0")
    Double getTotalRefundAmount();
    
    // Tìm payments cần refund (theo booking cancelled)
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'SUCCESS' AND p.booking.bookingStatus = 'CANCELLED'")
    List<Payment> findPaymentsNeedingRefund();
}
