package com.example.travelweb.controller;

import com.example.travelweb.entity.User;
import com.example.travelweb.entity.Tour;
import com.example.travelweb.entity.Category;
import com.example.travelweb.entity.Destination;
import com.example.travelweb.entity.Booking;
import com.example.travelweb.entity.Payment;
import com.example.travelweb.service.UserService;
import com.example.travelweb.service.TourService;
import com.example.travelweb.service.CategoryService;
import com.example.travelweb.service.DestinationService;
import com.example.travelweb.service.BookingService;
import com.example.travelweb.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private TourService tourService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private DestinationService destinationService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private PaymentService paymentService;

    // Dashboard chính
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        // Thống kê tổng quan
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalTours", tourService.getAllTours().size());
        model.addAttribute("totalCategories", categoryService.getAllCategories().size());
        model.addAttribute("totalDestinations", destinationService.getAllDestinations().size());
        
        // Thống kê theo role
        model.addAttribute("adminCount", userService.countUsersByRole(User.Role.ADMIN));
        model.addAttribute("staffCount", userService.countUsersByRole(User.Role.STAFF));
        model.addAttribute("customerCount", userService.countUsersByRole(User.Role.CUSTOMER));
        model.addAttribute("activeUsers", userService.countActiveUsers());
        
        // Thống kê booking và payment
        model.addAttribute("totalBookings", bookingService.countBookingsByStatus(Booking.BookingStatus.CONFIRMED));
        model.addAttribute("pendingBookings", bookingService.countBookingsByStatus(Booking.BookingStatus.PENDING));
        model.addAttribute("totalPayments", paymentService.countPaymentsByStatus(Payment.PaymentStatus.SUCCESS));
        
        // Dữ liệu gần đây
        model.addAttribute("recentUsers", userService.getAllUsers().stream().limit(5).toList());
        model.addAttribute("recentTours", tourService.getAllTours().stream().limit(5).toList());
        
        return "admin/dashboard";
    }

    // ==================== QUẢN LÝ NGƯỜI DÙNG ====================
    
    @GetMapping("/users")
    public String listUsers(Model model, 
                           @RequestParam(defaultValue = "") String search,
                           @RequestParam(required = false) String role) {
        List<User> users;
        
        if (!search.isEmpty()) {
            users = userService.searchUsersByName(search);
        } else if (role != null && !role.isEmpty()) {
            users = userService.getUsersByRole(User.Role.valueOf(role));
        } else {
            users = userService.getAllUsers();
        }
        
        model.addAttribute("users", users);
        model.addAttribute("search", search);
        model.addAttribute("selectedRole", role);
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("statuses", User.Status.values());
        
        return "admin/users/list";
    }

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        model.addAttribute("user", user);
        return "admin/users/detail";
    }

    @GetMapping("/users/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newUser(Model model) {
        model.addAttribute("user", new User());
        return "admin/users/form";
    }

    @GetMapping("/users/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        model.addAttribute("user", user);
        return "admin/users/form";
    }

    @PostMapping("/users/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveUser(@ModelAttribute User user, 
                          @RequestParam(required = false) String password,
                          @RequestParam(required = false) String confirmPassword,
                          RedirectAttributes redirectAttributes) {
        try {
            if (user.getUserId() == null) {
                // Tạo user mới
                if (password == null || password.trim().isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", "Mật khẩu không được để trống!");
                    return "redirect:/admin/users/new";
                }
                if (!password.equals(confirmPassword)) {
                    redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                    return "redirect:/admin/users/new";
                }
                userService.createUser(user, password);
                redirectAttributes.addFlashAttribute("success", "Đã tạo người dùng mới thành công!");
            } else {
                // Cập nhật user
                userService.updateUser(user.getUserId(), user);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật người dùng thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return user.getUserId() == null ? "redirect:/admin/users/new" : "redirect:/admin/users/" + user.getUserId() + "/edit";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUserStatus(@PathVariable Long id, 
                                  @RequestParam String status,
                                  RedirectAttributes redirectAttributes) {
        try {
            userService.changeUserStatus(id, User.Status.valueOf(status));
            redirectAttributes.addFlashAttribute("success", "Đã thay đổi trạng thái người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public String resetUserPassword(@PathVariable Long id, 
                                   @RequestParam String newPassword,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (newPassword == null || newPassword.trim().length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
                return "redirect:/admin/users/" + id;
            }
            userService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success", "Đã reset mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/change-role")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUserRole(@PathVariable Long id, 
                                @RequestParam String role,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.changeUserRole(id, User.Role.valueOf(role));
            redirectAttributes.addFlashAttribute("success", "Đã thay đổi quyền người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/change-status")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUserStatusOld(@PathVariable Long id, 
                                  @RequestParam String status,
                                  RedirectAttributes redirectAttributes) {
        try {
            userService.changeUserStatus(id, User.Status.valueOf(status));
            redirectAttributes.addFlashAttribute("success", "Đã thay đổi trạng thái người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ==================== QUẢN LÝ TOUR ====================
    
    @GetMapping("/tours")
    public String listTours(Model model, 
                           @RequestParam(defaultValue = "") String search,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) String status) {
        List<Tour> tours;
        
        if (!search.isEmpty()) {
            tours = tourService.searchToursByName(search);
        } else if (categoryId != null) {
            tours = tourService.getToursByCategory(categoryId);
        } else if (status != null && !status.isEmpty()) {
            // Assuming we have a method to get tours by status
            tours = tourService.getAllTours().stream()
                    .filter(tour -> tour.getStatus() != null && tour.getStatus().name().equals(status))
                    .toList();
        } else {
            tours = tourService.getAllTours();
        }
        
        model.addAttribute("tours", tours);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("search", search);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStatus", status);
        
        return "admin/tours/list";
    }

    @GetMapping("/tours/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newTour(Model model) {
        model.addAttribute("tour", new Tour());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("destinations", destinationService.getAllDestinations());
        return "admin/tours/form";
    }

    @GetMapping("/tours/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editTour(@PathVariable Long id, Model model) {
        Tour tour = tourService.getTourById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tour"));
        model.addAttribute("tour", tour);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("destinations", destinationService.getAllDestinations());
        return "admin/tours/form";
    }

    @PostMapping("/tours/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveTour(@ModelAttribute Tour tour, RedirectAttributes redirectAttributes, 
                          Authentication authentication) {
        try {
            if (tour.getTourId() == null) {
                // Set createdBy for new tour
                User currentUser = userService.findByUsername(authentication.getName());
                tour.setCreatedBy(currentUser);
                tourService.createTour(tour);
                redirectAttributes.addFlashAttribute("success", "Đã tạo tour mới thành công!");
            } else {
                tourService.updateTour(tour.getTourId(), tour);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật tour thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return tour.getTourId() == null ? "redirect:/admin/tours/new" : "redirect:/admin/tours/" + tour.getTourId() + "/edit";
        }
        return "redirect:/admin/tours";
    }

    @PostMapping("/tours/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteTour(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tourService.deleteTour(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa tour thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/tours";
    }

    // ==================== QUẢN LÝ DANH MỤC ====================
    
    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories/list";
    }

    @GetMapping("/categories/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    @GetMapping("/categories/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editCategory(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        model.addAttribute("category", category);
        return "admin/categories/form";
    }

    @PostMapping("/categories/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            if (category.getCategoryId() == null) {
                categoryService.createCategory(category);
                redirectAttributes.addFlashAttribute("success", "Đã tạo danh mục mới thành công!");
            } else {
                categoryService.updateCategory(category.getCategoryId(), category);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật danh mục thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa danh mục thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    // ==================== QUẢN LÝ ĐIỂM ĐẾN ====================
    
    @GetMapping("/destinations")
    public String listDestinations(Model model,
                                  @RequestParam(defaultValue = "") String search,
                                  @RequestParam(required = false) String country,
                                  @RequestParam(required = false) String status) {
        List<Destination> destinations;
        
        if (!search.isEmpty()) {
            destinations = destinationService.searchDestinationsByName(search);
        } else if (country != null && !country.isEmpty()) {
            destinations = destinationService.getDestinationsByCountry(country);
        } else if (status != null && !status.isEmpty()) {
            // Filter by status (Boolean field)
            Boolean statusValue = "ACTIVE".equals(status) ? true : false;
            destinations = destinationService.getAllDestinations().stream()
                    .filter(dest -> dest.getStatus() != null && dest.getStatus().equals(statusValue))
                    .toList();
        } else {
            destinations = destinationService.getAllDestinations();
        }
        
        // Get unique countries for filter dropdown
        List<String> countries = destinationService.getAllDestinations().stream()
                .map(Destination::getCountry)
                .distinct()
                .sorted()
                .toList();
        
        model.addAttribute("destinations", destinations);
        model.addAttribute("countries", countries);
        model.addAttribute("search", search);
        model.addAttribute("selectedCountry", country);
        model.addAttribute("selectedStatus", status);
        
        return "admin/destinations/list";
    }

    @GetMapping("/destinations/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newDestination(Model model) {
        model.addAttribute("destination", new Destination());
        return "admin/destinations/form";
    }

    @GetMapping("/destinations/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editDestination(@PathVariable Long id, Model model) {
        Destination destination = destinationService.getDestinationById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy điểm đến"));
        model.addAttribute("destination", destination);
        return "admin/destinations/form";
    }

    @PostMapping("/destinations/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveDestination(@ModelAttribute Destination destination, RedirectAttributes redirectAttributes) {
        try {
            if (destination.getDestinationId() == null) {
                destinationService.createDestination(destination);
                redirectAttributes.addFlashAttribute("success", "Đã tạo điểm đến mới thành công!");
            } else {
                destinationService.updateDestination(destination.getDestinationId(), destination);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật điểm đến thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/destinations";
    }

    @PostMapping("/destinations/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteDestination(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            destinationService.deleteDestination(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa điểm đến thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/destinations";
    }

    // ==================== QUẢN LÝ BOOKING (Existing functionality) ====================
    
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
        
        return "admin/bookings/list";
    }
    
    @GetMapping("/bookings/{id}")
    public String bookingDetail(@PathVariable Long id, Model model) {
        Booking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking"));
        
        List<Payment> payments = paymentService.getPaymentsByBooking(id);
        
        model.addAttribute("booking", booking);
        model.addAttribute("payments", payments);
        
        return "admin/bookings/detail";
    }
    
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
    
    @PostMapping("/bookings/{id}/cancel")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public String cancelBooking(@PathVariable Long id, @RequestParam String reason) {
        try {
            bookingService.cancelBooking(id, reason);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    @PostMapping("/bookings/{id}/complete")
    @ResponseBody
    public String completeBooking(@PathVariable Long id) {
        try {
            bookingService.completeBooking(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    // ==================== QUẢN LÝ PAYMENT ====================
    
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
        
        return "admin/payments/list";
    }
    
    @PostMapping("/payments/{id}/confirm")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public String confirmPayment(@PathVariable Long id, @RequestParam String response) {
        try {
            paymentService.confirmPayment(id, response);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    // ==================== THỐNG KÊ ====================
    
    @GetMapping("/statistics")
    public String statistics(Model model,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate,
                           @RequestParam(defaultValue = "month") String period) {
        
        try {
            // Thống kê tổng quan
            model.addAttribute("totalUsers", userService.getAllUsers().size());
            model.addAttribute("totalTours", tourService.getAllTours().size());
            model.addAttribute("totalBookings", bookingService.getAllBookings().size());
            model.addAttribute("totalCategories", categoryService.getAllCategories().size());
            model.addAttribute("totalDestinations", destinationService.getAllDestinations().size());
            
            // Thống kê theo role
            model.addAttribute("adminCount", userService.countUsersByRole(User.Role.ADMIN));
            model.addAttribute("staffCount", userService.countUsersByRole(User.Role.STAFF));
            model.addAttribute("customerCount", userService.countUsersByRole(User.Role.CUSTOMER));
            model.addAttribute("activeUsers", userService.countActiveUsers());
            
            // Thống kê booking theo status
            model.addAttribute("confirmedBookings", bookingService.countBookingsByStatus(Booking.BookingStatus.CONFIRMED));
            model.addAttribute("pendingBookings", bookingService.countBookingsByStatus(Booking.BookingStatus.PENDING));
            model.addAttribute("cancelledBookings", bookingService.countBookingsByStatus(Booking.BookingStatus.CANCELLED));
            model.addAttribute("completedBookings", bookingService.countBookingsByStatus(Booking.BookingStatus.COMPLETED));
            
            // Thống kê payment theo status
            model.addAttribute("successPayments", paymentService.countPaymentsByStatus(Payment.PaymentStatus.SUCCESS));
            model.addAttribute("pendingPayments", paymentService.countPaymentsByStatus(Payment.PaymentStatus.PENDING));
            model.addAttribute("failedPayments", paymentService.countPaymentsByStatus(Payment.PaymentStatus.FAILED));
            model.addAttribute("cancelledPayments", paymentService.countPaymentsByStatus(Payment.PaymentStatus.CANCELLED));
            
            // Doanh thu và thống kê thanh toán
            List<Object[]> revenueByMonth = bookingService.getRevenueByMonth(Booking.BookingStatus.CONFIRMED);
            List<Object[]> revenueByPaymentMethod = paymentService.getRevenueByPaymentMethod(Payment.PaymentStatus.SUCCESS);
            
            model.addAttribute("revenueByMonth", revenueByMonth != null ? revenueByMonth : new ArrayList<>());
            model.addAttribute("revenueByPaymentMethod", revenueByPaymentMethod != null ? revenueByPaymentMethod : new ArrayList<>());
            
            // Tính tổng doanh thu
            Double totalRevenue = 0.0;
            if (revenueByMonth != null && !revenueByMonth.isEmpty()) {
                totalRevenue = revenueByMonth.stream()
                        .mapToDouble(item -> item[2] != null ? ((Number) item[2]).doubleValue() : 0.0)
                        .sum();
            }
            model.addAttribute("totalRevenue", totalRevenue);
            
            // Top tours
            List<Tour> topTours = tourService.getTopTours();
            model.addAttribute("topTours", topTours != null ? topTours : new ArrayList<>());
            
            // Thống kê theo thời gian
            model.addAttribute("selectedPeriod", period);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            
            // Thống kê tours theo category
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories != null ? categories : new ArrayList<>());
            
            // Thống kê tours theo destination
            List<Destination> destinations = destinationService.getAllDestinations();
            model.addAttribute("destinations", destinations != null ? destinations : new ArrayList<>());
            
        } catch (Exception e) {
            // Xử lý lỗi và set default values
            model.addAttribute("error", "Có lỗi khi tải dữ liệu thống kê: " + e.getMessage());
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalTours", 0);
            model.addAttribute("totalBookings", 0);
            model.addAttribute("totalCategories", 0);
            model.addAttribute("totalDestinations", 0);
            model.addAttribute("adminCount", 0);
            model.addAttribute("staffCount", 0);
            model.addAttribute("customerCount", 0);
            model.addAttribute("activeUsers", 0);
            model.addAttribute("confirmedBookings", 0);
            model.addAttribute("pendingBookings", 0);
            model.addAttribute("cancelledBookings", 0);
            model.addAttribute("completedBookings", 0);
            model.addAttribute("successPayments", 0);
            model.addAttribute("pendingPayments", 0);
            model.addAttribute("failedPayments", 0);
            model.addAttribute("cancelledPayments", 0);
            model.addAttribute("revenueByMonth", new ArrayList<>());
            model.addAttribute("revenueByPaymentMethod", new ArrayList<>());
            model.addAttribute("totalRevenue", 0.0);
            model.addAttribute("topTours", new ArrayList<>());
            model.addAttribute("categories", new ArrayList<>());
            model.addAttribute("destinations", new ArrayList<>());
        }
        
        return "admin/statistics";
    }
    
    // API endpoint để lấy dữ liệu thống kê theo thời gian thực
    @GetMapping("/api/statistics/realtime")
    @ResponseBody
    public Map<String, Object> getRealtimeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Thống kê tổng quan
            stats.put("totalUsers", userService.getAllUsers().size());
            stats.put("totalTours", tourService.getAllTours().size());
            stats.put("totalBookings", bookingService.getAllBookings().size());
            stats.put("pendingBookings", bookingService.countBookingsByStatus(Booking.BookingStatus.PENDING));
            
            // Doanh thu hôm nay
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            List<Payment> todayPayments = paymentService.getSuccessfulPaymentsInDateRange(startOfDay, endOfDay, Payment.PaymentStatus.SUCCESS);
            Double todayRevenue = todayPayments.stream()
                    .mapToDouble(p -> p.getAmount().doubleValue())
                    .sum();
            stats.put("todayRevenue", todayRevenue);
            
        } catch (Exception e) {
            // Log error và return default values
            stats.put("totalUsers", 0);
            stats.put("totalTours", 0);
            stats.put("totalBookings", 0);
            stats.put("pendingBookings", 0);
            stats.put("todayRevenue", 0.0);
            stats.put("error", "Có lỗi khi lấy dữ liệu thống kê: " + e.getMessage());
        }
        
        return stats;
    }
    
    // API endpoint để lấy dữ liệu biểu đồ theo khoảng thời gian
    @GetMapping("/api/statistics/chart")
    @ResponseBody
    public Map<String, Object> getChartData(@RequestParam(required = false) String startDate,
                                          @RequestParam(required = false) String endDate,
                                          @RequestParam(defaultValue = "month") String period) {
        Map<String, Object> chartData = new HashMap<>();
        
        try {
            // Doanh thu theo tháng
            List<Object[]> revenueByMonth = bookingService.getRevenueByMonth(Booking.BookingStatus.CONFIRMED);
            chartData.put("revenueByMonth", revenueByMonth);
            
            // Doanh thu theo phương thức thanh toán
            List<Object[]> revenueByPaymentMethod = paymentService.getRevenueByPaymentMethod(Payment.PaymentStatus.SUCCESS);
            chartData.put("revenueByPaymentMethod", revenueByPaymentMethod);
            
            // Thống kê booking theo status
            Map<String, Long> bookingStats = new HashMap<>();
            bookingStats.put("CONFIRMED", bookingService.countBookingsByStatus(Booking.BookingStatus.CONFIRMED));
            bookingStats.put("PENDING", bookingService.countBookingsByStatus(Booking.BookingStatus.PENDING));
            bookingStats.put("CANCELLED", bookingService.countBookingsByStatus(Booking.BookingStatus.CANCELLED));
            bookingStats.put("COMPLETED", bookingService.countBookingsByStatus(Booking.BookingStatus.COMPLETED));
            chartData.put("bookingStats", bookingStats);
            
            // Thống kê user theo role
            Map<String, Long> userStats = new HashMap<>();
            userStats.put("ADMIN", userService.countUsersByRole(User.Role.ADMIN));
            userStats.put("STAFF", userService.countUsersByRole(User.Role.STAFF));
            userStats.put("CUSTOMER", userService.countUsersByRole(User.Role.CUSTOMER));
            chartData.put("userStats", userStats);
            
        } catch (Exception e) {
            chartData.put("error", "Có lỗi khi lấy dữ liệu biểu đồ: " + e.getMessage());
            chartData.put("revenueByMonth", new ArrayList<>());
            chartData.put("revenueByPaymentMethod", new ArrayList<>());
            chartData.put("bookingStats", new HashMap<>());
            chartData.put("userStats", new HashMap<>());
        }
        
        return chartData;
    }
}
