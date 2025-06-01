package com.example.travelweb.service;

import com.example.travelweb.entity.Payment;
import com.example.travelweb.entity.Booking;
import com.example.travelweb.repository.PaymentRepository;
import com.example.travelweb.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingService bookingService;
    
    // Lấy tất cả payments
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    // Lấy payment theo ID
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }
    
    // Lấy payment theo transaction ID
    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }
    
    // Lấy payments theo booking
    public List<Payment> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingBookingIdOrderByPaymentDateDesc(bookingId);
    }
    
    // Lấy payments theo payment method
    public List<Payment> getPaymentsByMethod(Payment.PaymentMethod method) {
        return paymentRepository.findByPaymentMethod(method);
    }
    
    // Lấy payments theo status
    public List<Payment> getPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status);
    }
    
    // Tạo payment mới
    public Payment createPayment(Payment payment) {
        // Validate booking
        Booking booking = bookingRepository.findById(payment.getBooking().getBookingId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
        
        // Kiểm tra số tiền payment không vượt quá số tiền còn lại
        BigDecimal totalPaid = getTotalPaidAmount(booking.getBookingId());
        BigDecimal remainingAmount = booking.getFinalAmount().subtract(totalPaid);
        
        if (payment.getAmount().compareTo(remainingAmount) > 0) {
            throw new RuntimeException("Số tiền thanh toán vượt quá số tiền còn lại");
        }
        
        // Generate transaction ID if not provided
        if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
            payment.setTransactionId(generateTransactionId());
        }
        
        // Set default status
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        }
        
        // Save payment
        Payment savedPayment = paymentRepository.save(payment);
        
        // Cập nhật payment status của booking
        updateBookingPaymentStatus(booking.getBookingId());
        
        return savedPayment;
    }
    
    // Cập nhật payment
    public Payment updatePayment(Long id, Payment paymentDetails) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment với ID: " + id));
        
        // Chỉ cho phép cập nhật một số trường
        payment.setPaymentDescription(paymentDetails.getPaymentDescription());
        payment.setGatewayResponse(paymentDetails.getGatewayResponse());
        
        return paymentRepository.save(payment);
    }
    
    // Xác nhận thanh toán thành công
    public Payment confirmPayment(Long id, String gatewayResponse) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment với ID: " + id));
        
        if (payment.getPaymentStatus() != Payment.PaymentStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể xác nhận payment đang ở trạng thái PENDING");
        }
        
        payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);
        payment.setProcessedDate(LocalDateTime.now());
        payment.setGatewayResponse(gatewayResponse);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Cập nhật payment status của booking
        updateBookingPaymentStatus(payment.getBooking().getBookingId());
        
        return savedPayment;
    }
    
    // Đánh dấu thanh toán thất bại
    public Payment failPayment(Long id, String reason) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment với ID: " + id));
        
        payment.setPaymentStatus(Payment.PaymentStatus.FAILED);
        payment.setProcessedDate(LocalDateTime.now());
        payment.setGatewayResponse(reason);
        
        return paymentRepository.save(payment);
    }
    
    // Hủy thanh toán
    public Payment cancelPayment(Long id, String reason) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment với ID: " + id));
        
        if (payment.getPaymentStatus() == Payment.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Không thể hủy payment đã thành công");
        }
        
        payment.setPaymentStatus(Payment.PaymentStatus.CANCELLED);
        payment.setProcessedDate(LocalDateTime.now());
        payment.setGatewayResponse("Cancelled: " + reason);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Cập nhật payment status của booking
        updateBookingPaymentStatus(payment.getBooking().getBookingId());
        
        return savedPayment;
    }
    
    // Hoàn tiền
    public Payment refundPayment(Long id, BigDecimal refundAmount, String reason) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment với ID: " + id));
        
        if (payment.getPaymentStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Chỉ có thể hoàn tiền cho payment đã thành công");
        }
        
        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new RuntimeException("Số tiền hoàn không thể lớn hơn số tiền đã thanh toán");
        }
        
        // Tạo payment mới cho việc hoàn tiền
        Payment refundPayment = new Payment();
        refundPayment.setBooking(payment.getBooking());
        refundPayment.setPaymentMethod(payment.getPaymentMethod());
        refundPayment.setAmount(refundAmount.negate()); // Số âm để đánh dấu là hoàn tiền
        refundPayment.setTransactionId(generateRefundTransactionId(payment.getTransactionId()));
        refundPayment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);
        refundPayment.setPaymentDescription("Hoàn tiền cho " + payment.getTransactionId() + ": " + reason);
        refundPayment.setProcessedDate(LocalDateTime.now());
        
        Payment savedRefund = paymentRepository.save(refundPayment);
        
        // Cập nhật payment status của booking
        updateBookingPaymentStatus(payment.getBooking().getBookingId());
        
        return savedRefund;
    }
    
    // Tính tổng số tiền đã thanh toán cho booking
    public BigDecimal getTotalPaidAmount(Long bookingId) {
        List<Payment> successPayments = paymentRepository.findSuccessfulPaymentsByBooking(bookingId);
        return successPayments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Tính số tiền còn lại cần thanh toán
    public BigDecimal getRemainingAmount(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
        
        BigDecimal totalPaid = getTotalPaidAmount(bookingId);
        return booking.getFinalAmount().subtract(totalPaid);
    }
    
    // Kiểm tra booking đã thanh toán đầy đủ chưa
    public boolean isBookingFullyPaid(Long bookingId) {
        BigDecimal remainingAmount = getRemainingAmount(bookingId);
        return remainingAmount.compareTo(BigDecimal.ZERO) <= 0;
    }
    
    // Lấy payments trong khoảng thời gian
    public List<Payment> getPaymentsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findPaymentsInDateRange(startDate, endDate);
    }
    
    // Lấy payments thành công trong khoảng thời gian
    public List<Payment> getSuccessfulPaymentsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findSuccessfulPaymentsInDateRange(startDate, endDate);
    }
    
    // Thống kê doanh thu theo phương thức thanh toán
    public List<Object[]> getRevenueByPaymentMethod() {
        return paymentRepository.getRevenueByPaymentMethod();
    }
    
    // Thống kê payment theo status
    public long countPaymentsByStatus(Payment.PaymentStatus status) {
        return paymentRepository.countByPaymentStatus(status);
    }
    
    // Thống kê payment theo status (alias cho AdminController)
    public long countByStatus(Payment.Status status) {
        return paymentRepository.countByPaymentStatus(Payment.PaymentStatus.valueOf(status.name()));
    }
    
    // Thống kê payment theo method
    public long countPaymentsByMethod(Payment.PaymentMethod method) {
        return paymentRepository.countByPaymentMethod(method);
    }
    
    // Private helper methods
    private String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = String.valueOf((int)(Math.random() * 10000));
        String transactionId = "TXN" + timestamp + String.format("%04d", Integer.parseInt(randomSuffix));
        
        // Kiểm tra xem transaction ID đã tồn tại chưa
        while (paymentRepository.existsByTransactionId(transactionId)) {
            randomSuffix = String.valueOf((int)(Math.random() * 10000));
            transactionId = "TXN" + timestamp + String.format("%04d", Integer.parseInt(randomSuffix));
        }
        
        return transactionId;
    }
    
    private String generateRefundTransactionId(String originalTransactionId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "REF" + timestamp + "_" + originalTransactionId;
    }
    
    private void updateBookingPaymentStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
        
        BigDecimal totalPaid = getTotalPaidAmount(bookingId);
        BigDecimal finalAmount = booking.getFinalAmount();
        
        Booking.PaymentStatus newPaymentStatus;
        
        if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            newPaymentStatus = Booking.PaymentStatus.UNPAID;
        } else if (totalPaid.compareTo(finalAmount) >= 0) {
            newPaymentStatus = Booking.PaymentStatus.PAID;
        } else {
            newPaymentStatus = Booking.PaymentStatus.PARTIAL;
        }
        
        // Cập nhật payment status của booking nếu có thay đổi
        if (booking.getPaymentStatus() != newPaymentStatus) {
            bookingService.updatePaymentStatus(bookingId, newPaymentStatus);
        }
    }
    
    // Kiểm tra transaction ID đã tồn tại
    public boolean existsByTransactionId(String transactionId) {
        return paymentRepository.existsByTransactionId(transactionId);
    }
    
    // Xử lý payment callback từ gateway
    public Payment processPaymentCallback(String transactionId, Payment.PaymentStatus status, String gatewayResponse) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment với transaction ID: " + transactionId));
        
        payment.setPaymentStatus(status);
        payment.setProcessedDate(LocalDateTime.now());
        payment.setGatewayResponse(gatewayResponse);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Cập nhật booking payment status
        updateBookingPaymentStatus(payment.getBooking().getBookingId());
        
        return savedPayment;
    }
    
    // Tạo payment cho booking mới (thanh toán đầy đủ)
    public Payment createFullPayment(Long bookingId, Payment.PaymentMethod paymentMethod, String description) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
        
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(booking.getFinalAmount());
        payment.setPaymentDescription(description);
        
        return createPayment(payment);
    }
    
    // Tạo payment cho booking mới (thanh toán cọc)
    public Payment createDepositPayment(Long bookingId, Payment.PaymentMethod paymentMethod, 
                                      BigDecimal depositAmount, String description) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
        
        if (depositAmount.compareTo(booking.getFinalAmount()) > 0) {
            throw new RuntimeException("Số tiền cọc không thể lớn hơn tổng tiền booking");
        }
        
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(depositAmount);
        payment.setPaymentDescription(description);
        
        return createPayment(payment);
    }
}
