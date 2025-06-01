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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

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
        // Log authentication information
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Dashboard access - User: {}, Authorities: {}", auth.getName(), auth.getAuthorities());
        logger.info("Has ROLE_ADMIN: {}", auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        logger.info("Has ROLE_STAFF: {}", auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STAFF")));
        
        // Lấy số lượng người dùng theo vai trò
        long adminCount = userService.countByRole(User.Role.ADMIN);
        long staffCount = userService.countByRole(User.Role.STAFF);
        long customerCount = userService.countByRole(User.Role.CUSTOMER);
        long inactiveUserCount = userService.countByStatus(User.Status.INACTIVE);
        
        // Lấy số lượng đặt tour theo trạng thái
        long pendingBookings = bookingService.countByStatus(Booking.Status.PENDING);
        long confirmedBookings = bookingService.countByStatus(Booking.Status.CONFIRMED);
        
        // Lấy số lượng thanh toán thành công
        long successPayments = paymentService.countByStatus(Payment.Status.SUCCESS);
        
        // Lấy danh sách người dùng mới nhất
        List<User> recentUsers = userService.findAll();
        
        // Lấy danh sách tour
        List<Tour> allTours = tourService.findAll();
        
        // Add to model
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("staffCount", staffCount);
        model.addAttribute("customerCount", customerCount);
        model.addAttribute("inactiveUserCount", inactiveUserCount);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("successPayments", successPayments);
        model.addAttribute("recentUsers", recentUsers);
        model.addAttribute("allTours", allTours);
        
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
                           @RequestParam(required = false) Long categoryId) {
        List<Tour> tours;
        
        if (!search.isEmpty()) {
            tours = tourService.searchToursByName(search);
        } else if (categoryId != null) {
            tours = tourService.getToursByCategory(categoryId);
        } else {
            tours = tourService.getAllTours();
        }
        
        model.addAttribute("tours", tours);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("search", search);
        model.addAttribute("selectedCategoryId", categoryId);
        
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
    public String saveTour(@ModelAttribute Tour tour, RedirectAttributes redirectAttributes) {
        try {
            if (tour.getTourId() == null) {
                tourService.createTour(tour);
                redirectAttributes.addFlashAttribute("success", "Đã tạo tour mới thành công!");
            } else {
                tourService.updateTour(tour.getTourId(), tour);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật tour thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
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
    public String listDestinations(Model model) {
        model.addAttribute("destinations", destinationService.getAllDestinations());
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
    public String statistics(Model model) {
        List<Object[]> revenueByMonth = bookingService.getRevenueByMonth();
        List<Object[]> revenueByPaymentMethod = paymentService.getRevenueByPaymentMethod();
        
        model.addAttribute("revenueByMonth", revenueByMonth);
        model.addAttribute("revenueByPaymentMethod", revenueByPaymentMethod);
        
        return "admin/statistics";
    }
}
