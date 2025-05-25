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
        // Check if data is already loaded
        if (categoryRepository.count() > 0) {
            return; // Data already exists, do nothing
        }

        // Create Categories
        Category domestic = new Category("Nội địa", "Các tour du lịch trong nước");
        domestic.setStatus(true);
        Category international = new Category("Quốc tế", "Các tour du lịch nước ngoài");
        international.setStatus(true);
        categoryRepository.saveAll(Arrays.asList(domestic, international));

        // Create Destinations
        Destination hanoi = new Destination("Hà Nội", "Việt Nam", "Hà Nội");
        hanoi.setDescription("Thủ đô ngàn năm văn hiến");
        hanoi.setStatus(true);

        Destination hcm = new Destination("TP. Hồ Chí Minh", "Việt Nam", "Hồ Chí Minh");
        hcm.setDescription("Thành phố năng động");
        hcm.setStatus(true);        destinationRepository.saveAll(Arrays.asList(hanoi, hcm));

        // Create Users first (needed for tour creation)
        User admin = new User("admin", "admin@example.com", passwordEncoder.encode("admin123"), "Admin User");
        admin.setFullName("Admin User"); // Using setFullName instead of setFirstName/setLastName
        admin.setRole(User.Role.ADMIN); // Using User.Role enum
        admin.setStatus(User.Status.ACTIVE);

        User customer = new User("customer", "customer@example.com", passwordEncoder.encode("customer123"), "Customer User");
        customer.setFullName("Customer User"); // Using setFullName
        customer.setRole(User.Role.CUSTOMER);
        customer.setStatus(User.Status.ACTIVE);
        customer.setDateOfBirth(LocalDate.of(1990, 5, 15));
        customer.setGender(User.Gender.Male);

        userRepository.saveAll(Arrays.asList(admin, customer));

        // Create Tours
        Tour tour1 = new Tour("Khám phá Hà Nội", "HN001", domestic, hanoi, 3, 15, new BigDecimal("3000000"));
        tour1.setDescription("Tham quan các điểm nổi bật của Hà Nội");
        tour1.setDiscountPercentage(new BigDecimal("10"));
        tour1.setDifficultyLevel(Tour.DifficultyLevel.Easy);
        tour1.setStatus(Tour.Status.ACTIVE);
        tour1.setFeatured(true);
        tour1.setMainImageUrl("/images/hanoi-tour.jpg");
        tour1.setCreatedBy(admin); // Set the createdBy field

        Tour tour2 = new Tour("Sài Gòn sôi động", "SG001", domestic, hcm, 4, 20, new BigDecimal("4500000"));
        tour2.setDescription("Trải nghiệm cuộc sống về đêm tại Sài Gòn");
        tour2.setDifficultyLevel(Tour.DifficultyLevel.Medium);
        tour2.setStatus(Tour.Status.ACTIVE);
        tour2.setFeatured(false);
        tour2.setMainImageUrl("/images/saigon-tour.jpg");
        tour2.setCreatedBy(admin); // Set the createdBy field

        tourRepository.saveAll(Arrays.asList(tour1, tour2));
    }
}
