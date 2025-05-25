package com.example.travelweb.repository;

import com.example.travelweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Tìm user theo username
    Optional<User> findByUsername(String username);
    
    // Tìm user theo email
    Optional<User> findByEmail(String email);
    
    // Tìm user theo username hoặc email
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // Kiểm tra username đã tồn tại
    boolean existsByUsername(String username);
    
    // Kiểm tra email đã tồn tại
    boolean existsByEmail(String email);
    
    // Tìm user theo role
    List<User> findByRole(User.Role role);
    
    // Tìm user theo status
    List<User> findByStatus(User.Status status);
    
    // Tìm user theo role và status
    List<User> findByRoleAndStatus(User.Role role, User.Status status);
    
    // Tìm user theo tên (tìm kiếm gần đúng)
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:name%")
    List<User> findByFullNameContaining(@Param("name") String name);
    
    // Đếm số user theo role
    long countByRole(User.Role role);
    
    // Đếm số user active
    long countByStatus(User.Status status);
}
