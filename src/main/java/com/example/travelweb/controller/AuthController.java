package com.example.travelweb.controller;

import com.example.travelweb.dto.UserRegistrationDto;
import com.example.travelweb.entity.User;
import com.example.travelweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // Hiển thị trang đăng nhập
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
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

        // Kiểm tra mật khẩu xác nhận
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "auth/register";
        }

        try {
            // Tạo user mới
            User user = userService.createUser(registrationDto);
            redirectAttributes.addFlashAttribute("success", 
                "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi đăng ký. Vui lòng thử lại!");
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
} 