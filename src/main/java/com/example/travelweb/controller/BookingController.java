package com.example.travelweb.controller;

import com.example.travelweb.service.BookingService;
import com.example.travelweb.service.PaymentService;
import com.example.travelweb.service.TourService;
import com.example.travelweb.entity.Booking;
import com.example.travelweb.entity.Payment;
import com.example.travelweb.entity.Tour;
import com.example.travelweb.entity.TourSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.travelweb.entity.User;
import com.example.travelweb.repository.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/bookings")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private TourService tourService;
    
    @Autowired
    private UserRepository userRepository;
    
    // Hiển thị form booking
    @GetMapping("/create")
    public String showBookingForm(@RequestParam Long scheduleId, Model model) {
        // Logic để hiển thị form booking
        model.addAttribute("scheduleId", scheduleId);
        return "booking/form";
    }
    
    // Xử lý tạo booking mới
    @PostMapping("/create")
    public String createBooking(@ModelAttribute Booking booking,
                               @RequestParam Long scheduleId,
                               @RequestParam int adultCount,
                               @RequestParam int childCount,
                               @RequestParam(required = false) String customerNotes,
                               RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin user đang đăng nhập từ SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đặt tour");
                return "redirect:/auth/login";
            }
            
            String username;
            if (authentication.getPrincipal() instanceof UserDetails) {
                username = ((UserDetails) authentication.getPrincipal()).getUsername();
            } else {
                username = authentication.getName();
            }
            
            // Tìm user theo username hoặc email
            User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin người dùng")));
            
            // Initialize objects properly
            TourSchedule schedule = new TourSchedule();
            schedule.setScheduleId(scheduleId);
            
            booking.setSchedule(schedule);
            booking.setUser(user);
            booking.setAdultCount(adultCount);
            booking.setChildCount(childCount);
            booking.setCustomerNotes(customerNotes);
            
            // Tạo booking
            Booking savedBooking = bookingService.createBooking(booking);
            
            redirectAttributes.addFlashAttribute("success", "Đặt tour thành công!");
            return "redirect:/bookings/my";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/bookings/create?scheduleId=" + scheduleId;
        }
    }
    
    // Chi tiết booking
    @GetMapping("/{id}")
    public String bookingDetail(@PathVariable Long id, Model model) {
        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        
        if (bookingOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy booking");
            return "error/404";
        }
        
        Booking booking = bookingOpt.get();
        
        // Lấy payments của booking
        var payments = paymentService.getPaymentsByBooking(id);
        
        // Tính số tiền còn lại
        BigDecimal remainingAmount = paymentService.getRemainingAmount(id);
        
        model.addAttribute("booking", booking);
        model.addAttribute("payments", payments);
        model.addAttribute("remainingAmount", remainingAmount);
        
        return "booking/detail";
    }
    
    // Tìm booking theo mã
    @GetMapping("/search")
    public String searchBooking(@RequestParam String bookingCode, Model model) {
        Optional<Booking> bookingOpt = bookingService.getBookingByCode(bookingCode);
        
        if (bookingOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy booking với mã: " + bookingCode);
            return "booking/search";
        }
        
        return "redirect:/bookings/" + bookingOpt.get().getBookingId();
    }
    
    // Hủy booking
    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                               @RequestParam String reason,
                               RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id, reason);
            redirectAttributes.addFlashAttribute("success", "Hủy booking thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/bookings/" + id;
    }
    
    // Tạo payment cho booking
    @PostMapping("/{id}/payment")
    public String createPayment(@PathVariable Long id,
                               @RequestParam String paymentMethod,
                               @RequestParam BigDecimal amount,
                               @RequestParam(required = false) String description,
                               RedirectAttributes redirectAttributes) {
        try {
            Payment payment = new Payment();
            payment.getBooking().setBookingId(id);
            payment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentMethod.toUpperCase()));
            payment.setAmount(amount);
            payment.setPaymentDescription(description);
            
            paymentService.createPayment(payment);
            
            redirectAttributes.addFlashAttribute("success", "Tạo payment thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/bookings/" + id;
    }
    
    // REST API: Lấy thông tin booking
    @GetMapping("/api/{id}")
    @ResponseBody
    public Booking getBookingApi(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
    
    // REST API: Cập nhật booking status
    @PutMapping("/api/{id}/status")
    @ResponseBody
    public String updateBookingStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
            
            switch (bookingStatus) {
                case CONFIRMED:
                    bookingService.confirmBooking(id);
                    break;
                case COMPLETED:
                    bookingService.completeBooking(id);
                    break;
                default:
                    return "Invalid status";
            }
            
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    // Hiển thị danh sách tour đã đặt của user
    @GetMapping("/my")
    public String myBookings(Model model) {
        // Lấy thông tin user đang đăng nhập từ SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        
        String username;
        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            username = authentication.getName();
        }
        
        // Tìm user theo username hoặc email
        User user = userRepository.findByUsername(username)
            .orElseGet(() -> userRepository.findByEmail(username)
            .orElse(null));
            
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("bookings", bookingService.getBookingsByUser(user.getUserId()));
        return "user/my-bookings";
    }
}
