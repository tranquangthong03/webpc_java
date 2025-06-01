package com.example.travelweb.service;

import com.example.travelweb.entity.User;
import com.example.travelweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Attempting to authenticate user: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", username);
                    return new UsernameNotFoundException("Không tìm thấy user với username: " + username);
                });

        // Kiểm tra trạng thái user
        if (user.getStatus() != User.Status.ACTIVE) {
            logger.warn("User account is not active: {}, status: {}", username, user.getStatus());
            throw new UsernameNotFoundException("Tài khoản đã bị khóa hoặc không hoạt động");
        }
        
        logger.info("User found: {}, role: {}, status: {}", username, user.getRole(), user.getStatus());
        logger.info("Password hash: {}", user.getPasswordHash());
        
        return new CustomUserPrincipal(user);
    }

    // Custom UserDetails implementation
    public static class CustomUserPrincipal implements UserDetails {
        private User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // Thêm role với prefix ROLE_
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
            
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getStatus() == User.Status.ACTIVE;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getStatus() == User.Status.ACTIVE;
        }

        // Getter để truy cập User entity
        public User getUser() {
            return user;
        }

        public Long getUserId() {
            return user.getUserId();
        }

        public String getFullName() {
            return user.getFullName();
        }

        public String getEmail() {
            return user.getEmail();
        }

        public User.Role getRole() {
            return user.getRole();
        }
    }
} 