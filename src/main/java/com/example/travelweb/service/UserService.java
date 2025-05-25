package com.example.travelweb.service;

import com.example.travelweb.dto.UserRegistrationDto;
import com.example.travelweb.entity.User;
import com.example.travelweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Lấy tất cả users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Lấy user theo ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    // Lấy user theo username
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    // Lấy user theo email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // Phương thức cho authentication - trả về User thay vì Optional
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    // Tạo user từ DTO đăng ký
    public User createUser(UserRegistrationDto registrationDto) {
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFullName(registrationDto.getFullName());
        user.setPhone(registrationDto.getPhone());
        user.setAddress(registrationDto.getAddress());
        user.setDateOfBirth(registrationDto.getDateOfBirth());
        user.setGender(registrationDto.getGender() != null ? 
            User.Gender.valueOf(registrationDto.getGender().toUpperCase()) : null);
        user.setRole(User.Role.CUSTOMER); // Mặc định là customer
        user.setStatus(User.Status.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    // Tạo user mới
    public User createUser(User user) {
        // Kiểm tra username và email đã tồn tại
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username đã tồn tại: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã tồn tại: " + user.getEmail());
        }
        
        return userRepository.save(user);
    }
    
    // Cập nhật user (overload method)
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    // Cập nhật user
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));
        
        // Kiểm tra username và email (nếu thay đổi)
        if (!user.getUsername().equals(userDetails.getUsername()) && 
            userRepository.existsByUsername(userDetails.getUsername())) {
            throw new RuntimeException("Username đã tồn tại: " + userDetails.getUsername());
        }
        
        if (!user.getEmail().equals(userDetails.getEmail()) && 
            userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email đã tồn tại: " + userDetails.getEmail());
        }
        
        // Cập nhật thông tin
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setFullName(userDetails.getFullName());
        user.setPhone(userDetails.getPhone());
        user.setAddress(userDetails.getAddress());
        user.setDateOfBirth(userDetails.getDateOfBirth());
        user.setGender(userDetails.getGender());
        user.setAvatarUrl(userDetails.getAvatarUrl());
        
        return userRepository.save(user);
    }
    
    // Đổi mật khẩu
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        User user = findByUsername(username);
        if (user == null) {
            return false;
        }
        
        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            return false;
        }
        
        // Cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        return true;
    }
    
    // Thay đổi role user
    public User changeUserRole(Long id, User.Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));
        
        user.setRole(role);
        return userRepository.save(user);
    }
    
    // Thay đổi status user
    public User changeUserStatus(Long id, User.Status status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));
        
        user.setStatus(status);
        return userRepository.save(user);
    }
    
    // Cập nhật last login
    public void updateLastLogin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + id));
        
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
    
    // Xóa user
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy user với ID: " + id);
        }
        userRepository.deleteById(id);
    }
    
    // Lấy users theo role
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }
    
    // Lấy users theo status
    public List<User> getUsersByStatus(User.Status status) {
        return userRepository.findByStatus(status);
    }
    
    // Tìm kiếm user theo tên
    public List<User> searchUsersByName(String name) {
        return userRepository.findByFullNameContaining(name);
    }
    
    // Kiểm tra username có tồn tại
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    // Kiểm tra email có tồn tại
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // Đếm số user theo role
    public long countUsersByRole(User.Role role) {
        return userRepository.countByRole(role);
    }
    
    // Đếm số user active
    public long countActiveUsers() {
        return userRepository.countByStatus(User.Status.ACTIVE);
    }
}
