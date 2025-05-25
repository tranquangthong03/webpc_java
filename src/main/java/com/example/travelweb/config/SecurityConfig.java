package com.example.travelweb.config;

import com.example.travelweb.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // Redirect đến trang login-success để xử lý logic redirect
            response.sendRedirect("/auth/login-success");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // ==================== PUBLIC ENDPOINTS ====================
                // Trang chủ và thông tin công khai
                .requestMatchers("/", "/home", "/about", "/contact").permitAll()
                
                // Xem tours và destinations (không cần đăng nhập)
                .requestMatchers("/tours", "/tours/*/view", "/tours/search").permitAll()
                .requestMatchers("/destinations", "/destinations/*/view").permitAll()
                .requestMatchers("/categories", "/categories/*/tours").permitAll()
                
                // Authentication endpoints
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                .requestMatchers("/auth/login-success").permitAll()
                
                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // For H2 console if needed
                
                // ==================== ADMIN ENDPOINTS ====================
                // Dashboard và quản lý chung (ADMIN + STAFF)
                .requestMatchers("/admin", "/admin/", "/admin/dashboard").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/admin/statistics").hasAnyRole("ADMIN", "STAFF")
                
                // Quản lý người dùng (chỉ ADMIN)
                .requestMatchers("/admin/users/**").hasRole("ADMIN")
                
                // Quản lý tours (ADMIN có thể thêm/sửa/xóa, STAFF chỉ xem)
                .requestMatchers("/admin/tours", "/admin/tours/list").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/admin/tours/new", "/admin/tours/*/edit", 
                               "/admin/tours/save", "/admin/tours/*/delete").hasRole("ADMIN")
                
                // Quản lý categories (chỉ ADMIN)
                .requestMatchers("/admin/categories/**").hasRole("ADMIN")
                
                // Quản lý destinations (chỉ ADMIN)
                .requestMatchers("/admin/destinations/**").hasRole("ADMIN")
                
                // Quản lý bookings (ADMIN + STAFF có thể xem, ADMIN có thể hủy)
                .requestMatchers("/admin/bookings", "/admin/bookings/list", 
                               "/admin/bookings/*/view").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/admin/bookings/*/confirm").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/admin/bookings/*/cancel").hasRole("ADMIN")
                
                // Quản lý payments (ADMIN + STAFF có thể xem, ADMIN có thể xác nhận)
                .requestMatchers("/admin/payments", "/admin/payments/list").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/admin/payments/*/confirm").hasRole("ADMIN")
                
                // ==================== USER ENDPOINTS ====================
                // Profile management (tất cả user đã đăng nhập)
                .requestMatchers("/auth/profile", "/auth/change-password").authenticated()
                
                // Booking endpoints (chỉ CUSTOMER và ADMIN)
                .requestMatchers("/bookings/new", "/bookings/create").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/bookings/my-bookings").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/bookings/*/view").authenticated()
                .requestMatchers("/bookings/*/cancel").hasAnyRole("CUSTOMER", "ADMIN")
                
                // Payment endpoints (chỉ CUSTOMER và ADMIN)
                .requestMatchers("/payments/**").hasAnyRole("CUSTOMER", "ADMIN")
                
                // ==================== PROTECTED ENDPOINTS ====================
                // Các endpoint khác cần authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/auth/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400) // 24 hours
                .userDetailsService(userDetailsService)
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**") // Disable CSRF for H2 console
            )
            .headers(headers -> headers
                .frameOptions().sameOrigin() // Allow frames from same origin for H2 console
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/auth/access-denied") // Trang báo lỗi khi không có quyền
            );

        return http.build();
    }
}
