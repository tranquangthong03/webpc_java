package com.example.travelweb.controller;

import com.example.travelweb.service.BookingService;
import com.example.travelweb.service.PaymentService;
import com.example.travelweb.entity.Booking;
import com.example.travelweb.entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private PaymentService paymentService;
    
    // Dashboard
    @GetMapping
    public String dashboard(Model model) {
        // Thống kê cơ bản
        long totalBookings = bookingService.countBookingsByStatus(Booking.BookingStatus.CONFIRMED);
        long pendingBookings = bookingService.countBookingsByStatus(Booking.BookingStatus.PENDING);
        long totalPayments = paymentService.countPaymentsByStatus(Payment.PaymentStatus.SUCCESS);
        
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("totalPayments", totalPayments);
        
        return "admin/dashboard";
    }
    
    // Quản lý Bookings
    @GetMapping("/bookings")
    public String listBookings(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String status) {
        
        Pageable pageable = PageRequest.of(page, size);
        List<Booking> bookings;
        
        if (status != null && !status.isEmpty()) {
            Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
            bookings = bookingService.getBookingsByStatus(bookingStatus);
        } else {
            bookings = bookingService.getAllBookings();
        }
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("currentPage", page);
        model.addAttribute("selectedStatus", status);
        
        return "admin/bookings";
    }
    
    // Chi tiết booking
    @GetMapping("/bookings/{id}")
    public String bookingDetail(@PathVariable Long id, Model model) {
        Booking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
        
        List<Payment> payments = paymentService.getPaymentsByBooking(id);
        
        model.addAttribute("booking", booking);
        model.addAttribute("payments", payments);
        
        return "admin/booking-detail";
    }
    
    // Xác nhận booking
    @PostMapping("/bookings/{id}/confirm")
    @ResponseBody
    public String confirmBooking(@PathVariable Long id) {
        try {
            bookingService.confirmBooking(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    // Hủy booking
    @PostMapping("/bookings/{id}/cancel")
    @ResponseBody
    public String cancelBooking(@PathVariable Long id, @RequestParam String reason) {
        try {
            bookingService.cancelBooking(id, reason);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    // Quản lý Payments
    @GetMapping("/payments")
    public String listPayments(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String status) {
        
        List<Payment> payments;
        
        if (status != null && !status.isEmpty()) {
            Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status.toUpperCase());
            payments = paymentService.getPaymentsByStatus(paymentStatus);
        } else {
            payments = paymentService.getAllPayments();
        }
        
        model.addAttribute("payments", payments);
        model.addAttribute("currentPage", page);
        model.addAttribute("selectedStatus", status);
        
        return "admin/payments";
    }
    
    // Xác nhận payment
    @PostMapping("/payments/{id}/confirm")
    @ResponseBody
    public String confirmPayment(@PathVariable Long id, @RequestParam String response) {
        try {
            paymentService.confirmPayment(id, response);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    // Thống kê
    @GetMapping("/statistics")
    public String statistics(Model model) {
        List<Object[]> revenueByMonth = bookingService.getRevenueByMonth();
        List<Object[]> revenueByPaymentMethod = paymentService.getRevenueByPaymentMethod();
        
        model.addAttribute("revenueByMonth", revenueByMonth);
        model.addAttribute("revenueByPaymentMethod", revenueByPaymentMethod);
        
        return "admin/statistics";
    }
}
