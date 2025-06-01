package com.example.travelweb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class TestController {
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello World! Spring Boot is working!");
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Application is running successfully!");
    }
    
    @GetMapping("/api/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("{\"status\":\"OK\",\"message\":\"Travel Web API is running\"}");
    }
    
    @GetMapping("/api/auth-check")
    public ResponseEntity<Map<String, Object>> authCheck() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        
        response.put("authenticated", auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser"));
        response.put("username", auth.getName());
        response.put("authorities", auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }
}
