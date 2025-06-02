package com.example.travelweb.service;

import com.example.travelweb.entity.Booking;
import com.example.travelweb.entity.Booking.BookingStatus;
import com.example.travelweb.entity.TourSchedule;
import com.example.travelweb.entity.User;
import com.example.travelweb.entity.BookingParticipant;
import com.example.travelweb.entity.Payment;
import com.example.travelweb.repository.BookingRepository;
import com.example.travelweb.repository.TourScheduleRepository;
import com.example.travelweb.repository.UserRepository;
import com.example.travelweb.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@Transactional
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private TourScheduleRepository scheduleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    // Lấy tất cả bookings
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    // Lấy booking theo ID
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }
    
    // Lấy booking theo mã booking
    public Optional<Booking> getBookingByCode(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode);
    }
    
    // Lấy bookings theo user
    public List<Booking> getBookingsByUser(Long userId) {
        return bookingRepository.findByUserUserIdOrderByBookingDateDesc(userId);
    }
    
    // Lấy bookings theo user với phân trang
    public Page<Booking> getBookingsByUser(Long userId, Pageable pageable) {
        return bookingRepository.findByUserUserIdOrderByBookingDateDesc(userId, pageable);
    }
    
    // Lấy bookings theo status
    public List<Booking> getBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByBookingStatus(status);
    }
    
    // Lấy bookings theo payment status
    public List<Booking> getBookingsByPaymentStatus(Booking.PaymentStatus paymentStatus) {
        return bookingRepository.findByPaymentStatus(paymentStatus);
    }
    
    // Tạo booking mới
    public Booking createBooking(Booking booking) {
        // Validate tour schedule
        TourSchedule schedule = scheduleRepository.findById(booking.getSchedule().getScheduleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch trình tour"));
        
        // Kiểm tra số chỗ trống
        int requestedSlots = booking.getAdultCount() + booking.getChildCount();
        if (!schedule.hasAvailableSpace(requestedSlots)) {
            throw new RuntimeException("Không đủ chỗ trống cho tour này");
        }
        
        // Validate user
        User user = userRepository.findById(booking.getUser().getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        
        // Generate booking code
        if (booking.getBookingCode() == null || booking.getBookingCode().isEmpty()) {
            booking.setBookingCode(generateBookingCode());
        }
        
        // Tính toán số tiền
        BigDecimal totalAmount = calculateTotalAmount(schedule, booking.getAdultCount(), booking.getChildCount());
        booking.setTotalAmount(totalAmount);
        
        // Áp dụng giảm giá nếu có
        if (booking.getDiscountAmount() != null && booking.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            booking.setFinalAmount(totalAmount.subtract(booking.getDiscountAmount()));
        } else {
            booking.setFinalAmount(totalAmount);
        }
        
        // Set default values
        booking.setBookingStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Booking.PaymentStatus.UNPAID);
        
        // Save booking
        Booking savedBooking = bookingRepository.save(booking);
        
        // Cập nhật số chỗ đã book trong schedule
        schedule.setBookedSlots(schedule.getBookedSlots() + requestedSlots);
        scheduleRepository.save(schedule);
        
        return savedBooking;
    }
    
    // Cập nhật booking
    public Booking updateBooking(Long id, Booking bookingDetails) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + id));
        
        // Chỉ cho phép cập nhật một số trường
        booking.setCustomerNotes(bookingDetails.getCustomerNotes());
        booking.setAdminNotes(bookingDetails.getAdminNotes());
        
        // Cập nhật participant count nếu booking vẫn ở trạng thái PENDING
        if (booking.getBookingStatus() == Booking.BookingStatus.PENDING) {
            int oldTotalSlots = booking.getAdultCount() + booking.getChildCount();
            int newTotalSlots = bookingDetails.getAdultCount() + bookingDetails.getChildCount();
            
            // Kiểm tra availability
            TourSchedule schedule = booking.getSchedule();
            int availableSpace = schedule.getAvailableSpace() + oldTotalSlots; // Cộng lại số slot cũ
            
            if (availableSpace < newTotalSlots) {
                throw new RuntimeException("Không đủ chỗ trống cho số lượng người mới");
            }
            
            booking.setAdultCount(bookingDetails.getAdultCount());
            booking.setChildCount(bookingDetails.getChildCount());
            
            // Tính lại số tiền
            BigDecimal newTotalAmount = calculateTotalAmount(schedule, booking.getAdultCount(), booking.getChildCount());
            booking.setTotalAmount(newTotalAmount);
            booking.setFinalAmount(newTotalAmount.subtract(booking.getDiscountAmount()));
            
            // Cập nhật booked slots
            schedule.setBookedSlots(schedule.getBookedSlots() - oldTotalSlots + newTotalSlots);
            scheduleRepository.save(schedule);
        }
        
        return bookingRepository.save(booking);
    }
    
    // Xác nhận booking
    public Booking confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + id));
        
        if (booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể xác nhận booking đang ở trạng thái PENDING");
        }
        
        booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }
    
    // Hủy booking
    public Booking cancelBooking(Long id, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + id));
        
        if (booking.getBookingStatus() == Booking.BookingStatus.CANCELLED ||
            booking.getBookingStatus() == Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Không thể hủy booking ở trạng thái hiện tại");
        }
        
        booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelledReason(reason);
        
        // Trả lại số chỗ cho schedule
        TourSchedule schedule = booking.getSchedule();
        int releasedSlots = booking.getAdultCount() + booking.getChildCount();
        schedule.setBookedSlots(schedule.getBookedSlots() - releasedSlots);
        scheduleRepository.save(schedule);
        
        return bookingRepository.save(booking);
    }
    
    // Hoàn thành booking
    public Booking completeBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + id));
        
        if (booking.getBookingStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể hoàn thành booking đã được xác nhận");
        }
        
        booking.setBookingStatus(Booking.BookingStatus.COMPLETED);
        return bookingRepository.save(booking);
    }
    
    // Cập nhật payment status
    public Booking updatePaymentStatus(Long id, Booking.PaymentStatus paymentStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + id));
        
        booking.setPaymentStatus(paymentStatus);
        return bookingRepository.save(booking);
    }
    
    // Áp dụng giảm giá
    public Booking applyDiscount(Long id, BigDecimal discountAmount) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + id));
        
        if (discountAmount.compareTo(booking.getTotalAmount()) > 0) {
            throw new RuntimeException("Số tiền giảm giá không thể lớn hơn tổng tiền");
        }
        
        booking.setDiscountAmount(discountAmount);
        booking.setFinalAmount(booking.getTotalAmount().subtract(discountAmount));
        
        return bookingRepository.save(booking);
    }
      // Lấy bookings trong khoảng thời gian
    public List<Booking> getBookingsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return bookingRepository.findByBookingDateBetween(startDate, endDate);
    }
    
    // Lấy doanh thu theo tháng
    public List<Object[]> getRevenueByMonth() {
        return bookingRepository.getRevenueByMonth();
    }
    
    // Thống kê booking theo status
    public long countBookingsByStatus(Booking.BookingStatus status) {
        return bookingRepository.countByBookingStatus(status);
    }
    
    // Thống kê booking theo status (alias cho AdminController)
    public long countByStatus(Booking.Status status) {
        return bookingRepository.countByBookingStatus(Booking.BookingStatus.valueOf(status.name()));
    }
    
    // Thống kê booking theo user
    public long countBookingsByUser(Long userId) {
        return bookingRepository.countByUserUserId(userId);
    }
    
    // Private helper methods
    private String generateBookingCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = String.valueOf((int)(Math.random() * 1000));
        String bookingCode = "BK" + timestamp + String.format("%03d", Integer.parseInt(randomSuffix));
        
        // Kiểm tra xem code đã tồn tại chưa
        while (bookingRepository.existsByBookingCode(bookingCode)) {
            randomSuffix = String.valueOf((int)(Math.random() * 1000));
            bookingCode = "BK" + timestamp + String.format("%03d", Integer.parseInt(randomSuffix));
        }
        
        return bookingCode;
    }
    
    private BigDecimal calculateTotalAmount(TourSchedule schedule, int adultCount, int childCount) {
        BigDecimal adultPrice = schedule.getPrice();
        BigDecimal childPrice = adultPrice.multiply(new BigDecimal("0.7")); // Trẻ em giảm 30%
        
        BigDecimal totalAdultAmount = adultPrice.multiply(new BigDecimal(adultCount));
        BigDecimal totalChildAmount = childPrice.multiply(new BigDecimal(childCount));
        
        return totalAdultAmount.add(totalChildAmount);
    }
    
    // Kiểm tra booking code đã tồn tại
    public boolean existsByBookingCode(String bookingCode) {
        return bookingRepository.existsByBookingCode(bookingCode);
    }
    
    // Lấy bookings theo user và status
    public List<Booking> getBookingsByUserAndStatus(Long userId, Booking.BookingStatus status) {
        return bookingRepository.findByUserUserIdAndBookingStatus(userId, status);
    }
    
    // Lấy tất cả bookings theo user cho admin
    public List<Object[]> getBookingsByUserForAdmin() {
        List<User> users = userRepository.findAll();
        List<Object[]> results = new ArrayList<>();
        
        for (User user : users) {
            List<Booking> userBookings = bookingRepository.findByUserUserIdOrderByBookingDateDesc(user.getUserId());
            if (!userBookings.isEmpty()) {
                results.add(new Object[] { user, userBookings });
            }
        }
        
        return results;
    }
    
    // Thêm participant vào booking
    public Booking addParticipant(Long bookingId, BookingParticipant participant) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + bookingId));
        
        participant.setBooking(booking);
        booking.getParticipants().add(participant);
        
        return bookingRepository.save(booking);
    }
    
    // Xóa booking
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với ID: " + id));
        
        // Chỉ cho phép xóa booking đã hủy
        if (booking.getBookingStatus() != Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Chỉ có thể xóa booking đã hủy");
        }
        
        try {
            // Get all payments related to this booking
            List<Payment> payments = paymentRepository.findByBookingBookingIdOrderByPaymentDateDesc(id);
            
            // Delete each payment individually first
            if (!payments.isEmpty()) {
                for (Payment payment : payments) {
                    paymentRepository.delete(payment);
                }
                // Flush to ensure all payments are deleted
                paymentRepository.flush();
            }
            
            // Now it's safe to delete the booking
            bookingRepository.delete(booking);
            
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xóa booking: " + e.getMessage(), e);
        }
    }
}
