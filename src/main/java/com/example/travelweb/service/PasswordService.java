package com.example.travelweb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PasswordService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Kiểm tra xem mật khẩu có khớp với hash không
     * @param rawPassword Mật khẩu gốc
     * @param encodedPassword Hash đã mã hóa
     * @return true nếu mật khẩu khớp
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        boolean result = passwordEncoder.matches(rawPassword, encodedPassword);
        logger.debug("Password match check: {}", result);
        return result;
    }

    /**
     * Mã hóa mật khẩu
     * @param rawPassword Mật khẩu gốc
     * @return Mật khẩu đã mã hóa
     */
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
} 