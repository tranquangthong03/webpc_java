package com.example.travelweb.util;

import com.example.travelweb.entity.User;
import com.example.travelweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AdminAccountCreator implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createAdminAccount();
    }

    private void createAdminAccount() {
        try {
            // Kiểm tra xem đã có admin chưa
            if (userRepository.findByUsername("admin").isPresent()) {
                System.out.println("Admin account already exists!");
                
                // Cập nhật mật khẩu cho admin
                User admin = userRepository.findByUsername("admin").get();
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                userRepository.save(admin);
                System.out.println("Admin password updated to: admin123");
                return;
            }

            // Tạo tài khoản admin mới
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@travelweb.com");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setFullName("Administrator");
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(User.Status.ACTIVE);
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());

            userRepository.save(admin);
            System.out.println("=".repeat(50));
            System.out.println("ADMIN ACCOUNT CREATED SUCCESSFULLY!");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("Email: admin@travelweb.com");
            System.out.println("Role: ADMIN");
            System.out.println("=".repeat(50));

        } catch (Exception e) {
            System.err.println("Error creating admin account: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 