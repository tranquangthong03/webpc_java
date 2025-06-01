package com.example.travelweb.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTestUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Tạo hash mới cho mật khẩu admin
        String newPassword = "admin123"; // Thay đổi mật khẩu bạn muốn ở đây
        String newHash = encoder.encode(newPassword);
        
        System.out.println("=== HASH MỚI CHO TÀI KHOẢN ADMIN ===");
        System.out.println("Mật khẩu: " + newPassword);
        System.out.println("Hash: " + newHash);
        System.out.println();
        
        System.out.println("=== CÂU LỆNH SQL UPDATE ===");
        System.out.println("UPDATE Users SET password_hash = '" + newHash + "' WHERE username = 'admin';");
        System.out.println();
        
        // Verify hash works
        System.out.println("=== KIỂM TRA ===");
        System.out.println("Matches '" + newPassword + "': " + encoder.matches(newPassword, newHash));
        
        // Test với hash cũ
        String oldHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXYLFSaZNqsoYgGvZCTrAp/YCFG";
        System.out.println("\nHash cũ vẫn không match với 'admin123': " + encoder.matches("admin123", oldHash));
    }
} 