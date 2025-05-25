package com.example.travelweb.config;

import com.example.travelweb.entity.*;
import com.example.travelweb.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    DestinationRepository destinationRepository;
    @Autowired
    TourRepository tourRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // DataLoader disabled - using SQL Server database with data from travelDb.sql
        // Data is already loaded in SQL Server, no need to create sample data
        System.out.println("DataLoader: Using SQL Server database - sample data creation skipped");
    }
}
