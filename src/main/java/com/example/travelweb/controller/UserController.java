package com.example.travelweb.controller;

import com.example.travelweb.entity.User;
import com.example.travelweb.entity.Booking;
import com.example.travelweb.service.UserService;
import com.example.travelweb.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private BookingService bookingService;

    // Dashboard người dùng
    @GetMapping("/dashboard")
    public String userDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // Lấy thống kê của user
        List<Booking> userBookings = bookingService.getBookingsByUser(user.getUserId());
        long totalBookings = userBookings.size();
        long confirmedBookings = userBookings.stream()
                .mapToLong(b -> b.getBookingStatus() == Booking.BookingStatus.CONFIRMED ? 1 : 0)
                .sum();
        long pendingBookings = userBookings.stream()
                .mapToLong(b -> b.getBookingStatus() == Booking.BookingStatus.PENDING ? 1 : 0)
                .sum();
        
        model.addAttribute("user", user);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("recentBookings", userBookings.stream().limit(5).toList());
        
        return "user/dashboard";
    }

    // Xem danh sách booking của user
    @GetMapping("/bookings")
    public String myBookings(Authentication authentication, Model model,
                            @RequestParam(required = false) String status) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        List<Booking> bookings;
        if (status != null && !status.isEmpty()) {
            Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
            bookings = bookingService.getBookingsByUserAndStatus(user.getUserId(), bookingStatus);
        } else {
            bookings = bookingService.getBookingsByUser(user.getUserId());
        }
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", Booking.BookingStatus.values());
        
        return "user/bookings";
    }

    // Xem chi tiết booking
    @GetMapping("/bookings/{id}")
    public String bookingDetail(@PathVariable Long id, Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        Booking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
        
        // Kiểm tra quyền truy cập (chỉ user sở hữu booking hoặc admin)
        if (!booking.getUser().getUserId().equals(user.getUserId()) && 
            !user.getRole().equals(User.Role.ADMIN)) {
            return "redirect:/auth/access-denied";
        }
        
        model.addAttribute("booking", booking);
        
        return "user/booking-detail";
    }

    // Hủy booking
    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, 
                               @RequestParam String reason,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        try {
            Booking booking = bookingService.getBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
            
            // Kiểm tra quyền hủy
            if (!booking.getUser().getUserId().equals(user.getUserId()) && 
                !user.getRole().equals(User.Role.ADMIN)) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy booking này!");
                return "redirect:/user/bookings";
            }
            
            bookingService.cancelBooking(id, reason);
            redirectAttributes.addFlashAttribute("success", "Đã hủy booking thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/user/bookings";
    }

    // Xem lịch sử thanh toán
    @GetMapping("/payments")
    public String myPayments(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // Lấy tất cả payments của user thông qua bookings
        List<Booking> userBookings = bookingService.getBookingsByUser(user.getUserId());
        
        model.addAttribute("userBookings", userBookings);
        
        return "user/payments";
    }

    // Quản lý thông tin cá nhân
    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        model.addAttribute("user", user);
        
        return "user/profile";
    }

    // Cập nhật thông tin cá nhân
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User userDetails,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        try {
            // Cập nhật thông tin (không thay đổi username, email, role)
            user.setFullName(userDetails.getFullName());
            user.setPhone(userDetails.getPhone());
            user.setAddress(userDetails.getAddress());
            user.setDateOfBirth(userDetails.getDateOfBirth());
            user.setGender(userDetails.getGender());
            
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/user/profile";
    }

    // Đổi mật khẩu
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không khớp!");
            return "redirect:/user/profile";
        }
        
        try {
            boolean success = userService.changePassword(username, currentPassword, newPassword);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Đã đổi mật khẩu thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/user/profile";
    }

    // Tìm kiếm tours yêu thích (wishlist) - có thể mở rộng sau
    @GetMapping("/wishlist")
    public String wishlist(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        
        if (user == null) {
            return "redirect:/auth/login";
        }
        
        // TODO: Implement wishlist functionality
        model.addAttribute("message", "Tính năng wishlist sẽ được phát triển trong tương lai");
        
        return "user/wishlist";
    }
} 