package com.example.travelweb.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

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
}
