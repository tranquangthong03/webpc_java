package com.example.travelweb.controller;

import com.example.travelweb.dto.UserRegistrationDto;
import com.example.travelweb.entity.User;
import com.example.travelweb.service.UserService;
import com.example.travelweb.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordService passwordService;

    // Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
            
            // Debug: Kiểm tra user trong database
            logger.info("Login error occurred - checking database for admin user");
            User adminUser = userService.findByUsername("admin");
            if (adminUser != null) {
                logger.info("Admin user found: {}", adminUser.getUsername());
                logger.info("Admin status: {}", adminUser.getStatus());
                logger.info("Admin role: {}", adminUser.getRole());
                logger.info("Password hash length: {}", adminUser.getPasswordHash().length());
                
                // Thử kiểm tra mật khẩu "admin123" với hash hiện tại
                boolean matches = passwordService.matches("admin123", adminUser.getPasswordHash());
                logger.info("Password 'admin123' matches stored hash: {}", matches);
            } else {
                logger.warn("Admin user not found in database!");
            }
        }
        if (logout != null) {
            model.addAttribute("message", "Đăng xuất thành công!");
        }
        return "auth/login";
    }

    // Hiển thị trang đăng ký
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    // Xử lý đăng ký
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        // Kiểm tra validation errors
        if (result.hasErrors()) {
            model.addAttribute("error", "Vui lòng kiểm tra và điền đầy đủ thông tin bắt buộc!");
            return "auth/register";
        }

        // Kiểm tra mật khẩu xác nhận
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "auth/register";
        }

        // Kiểm tra email đã tồn tại
        if (userService.findByEmail(registrationDto.getEmail()) != null) {
            model.addAttribute("error", "Email đã được sử dụng!");
            return "auth/register";
        }

        // Kiểm tra username đã tồn tại
        if (userService.findByUsername(registrationDto.getUsername()) != null) {
            model.addAttribute("error", "Tên đăng nhập đã được sử dụng!");
            return "auth/register";
        }

        try {
            // Tạo user mới
            User user = userService.createUser(registrationDto);
            logger.info("User registered successfully: {}", user.getUsername());
            logger.info("Password hash: {}", user.getPasswordHash());
            
            redirectAttributes.addFlashAttribute("success", 
                "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            logger.error("Error during registration", e);
            model.addAttribute("error", "Có lỗi xảy ra khi đăng ký: " + e.getMessage());
            return "auth/register";
        }
    }

    // Xử lý sau khi đăng nhập thành công
    @GetMapping("/login-success")
    public String loginSuccess(HttpServletRequest request) {
        // Lấy thông tin user từ session
        String username = request.getRemoteUser();
        if (username != null) {
            User user = userService.findByUsername(username);
            if (user != null) {
                // Cập nhật last login
                userService.updateLastLogin(user.getUserId());
                logger.info("User logged in successfully: {}", username);
                
                // Redirect theo role
                if (user.getRole() == User.Role.ADMIN) {
                    return "redirect:/admin/dashboard";
                } else if (user.getRole() == User.Role.STAFF) {
                    return "redirect:/admin/dashboard";
                } else {
                    return "redirect:/";
                }
            }
        }
        return "redirect:/";
    }

    // Trang profile user
    @GetMapping("/profile")
    public String showProfile(Model model, HttpServletRequest request) {
        String username = request.getRemoteUser();
        if (username != null) {
            User user = userService.findByUsername(username);
            model.addAttribute("user", user);
            return "auth/profile";
        }
        return "redirect:/auth/login";
    }

    // Cập nhật profile
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User user,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        String username = request.getRemoteUser();
        if (username != null) {
            User currentUser = userService.findByUsername(username);
            if (currentUser != null) {
                // Cập nhật thông tin (không cho phép thay đổi username, email, role)
                currentUser.setFullName(user.getFullName());
                currentUser.setPhone(user.getPhone());
                currentUser.setAddress(user.getAddress());
                currentUser.setDateOfBirth(user.getDateOfBirth());
                currentUser.setGender(user.getGender());
                
                userService.updateUser(currentUser);
                redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
            }
        }
        return "redirect:/auth/profile";
    }

    // Đổi mật khẩu
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        
        String username = request.getRemoteUser();
        if (username == null) {
            return "redirect:/auth/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không khớp!");
            return "redirect:/auth/profile";
        }

        try {
            boolean success = userService.changePassword(username, currentPassword, newPassword);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đổi mật khẩu!");
        }

        return "redirect:/auth/profile";
    }
    
    // Endpoint để reset mật khẩu admin (chỉ dùng cho development)
    @GetMapping("/reset-admin")
    @ResponseBody
    public String resetAdminPassword() {
        try {
            User adminUser = userService.findByUsername("admin");
            if (adminUser != null) {
                // Đặt mật khẩu mới là "admin123"
                String newPasswordHash = passwordService.encode("admin123");
                adminUser.setPasswordHash(newPasswordHash);
                adminUser.setStatus(User.Status.ACTIVE);
                userService.updateUser(adminUser);
                
                return "Admin password reset successfully. New password: admin123";
            } else {
                return "Admin user not found!";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // Endpoint để tạo tài khoản admin mới nếu không tồn tại
    @GetMapping("/create-admin")
    @ResponseBody
    public String createAdminAccount() {
        try {
            User adminUser = userService.findByUsername("admin");
            if (adminUser == null) {
                // Tạo tài khoản admin mới
                User newAdmin = new User();
                newAdmin.setUsername("admin");
                newAdmin.setEmail("admin@example.com");
                newAdmin.setPasswordHash(passwordService.encode("admin123"));
                newAdmin.setFullName("Administrator");
                newAdmin.setRole(User.Role.ADMIN);
                newAdmin.setStatus(User.Status.ACTIVE);
                newAdmin.setCreatedAt(LocalDateTime.now());
                
                userService.createUser(newAdmin);
                return "Admin account created successfully. Username: admin, Password: admin123";
            } else {
                return "Admin account already exists!";
            }
        } catch (Exception e) {
            logger.error("Error creating admin account", e);
            return "Error: " + e.getMessage();
        }
    }
} 