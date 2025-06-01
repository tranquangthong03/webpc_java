package com.example.travelweb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Tours")
public class Tour {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tour_id")
    private Long tourId;
    
    @Column(name = "tour_name", nullable = false, length = 200)
    @NotBlank
    private String tourName;
    
    @Column(name = "tour_code", nullable = false, unique = true, length = 20)
    @NotBlank
    private String tourCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = true)
    private Destination destination;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "duration_days", nullable = false)
    @Min(1)
    private Integer durationDays;
    
    @Column(name = "max_participants", nullable = false)
    @Min(1)
    private Integer maxParticipants;
    
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @Column(name = "main_image_url")
    private String mainImageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", length = 20)
    private DifficultyLevel difficultyLevel;
    
    @Column(name = "includes", length = 1000)
    private String includes;
    
    @Column(name = "excludes", length = 1000)
    private String excludes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.ACTIVE;
    
    @Column(name = "featured", nullable = false)
    private Boolean featured = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Quan hệ One-to-Many
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TourSchedule> schedules = new ArrayList<>();
    
    // Tạm thời comment lại phần này để tránh lỗi với bảng tour_images không tồn tại
    /*
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TourImage> images = new ArrayList<>();
    */
    
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TourItinerary> itineraries = new ArrayList<>();
    
    // Enums
    public enum DifficultyLevel {
        Easy, Medium, Hard
    }
    
    public enum Status {
        ACTIVE, INACTIVE, DRAFT
    }
    
    // Constructors
    public Tour() {}
    
    public Tour(String tourName, String tourCode, Category category, Destination destination, 
                Integer durationDays, Integer maxParticipants, BigDecimal price) {
        this.tourName = tourName;
        this.tourCode = tourCode;
        this.category = category;
        this.destination = destination;
        this.durationDays = durationDays;
        this.maxParticipants = maxParticipants;
        this.price = price;
    }
    
    // Business methods
    public BigDecimal getDiscountedPrice() {
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = price.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
            return price.subtract(discount);
        }
        return price;
    }
    
    // Getters and Setters
    public Long getTourId() {
        return tourId;
    }
    
    public void setTourId(Long tourId) {
        this.tourId = tourId;
    }
    
    public String getTourName() {
        return tourName;
    }
    
    public void setTourName(String tourName) {
        this.tourName = tourName;
    }
    
    public String getTourCode() {
        return tourCode;
    }
    
    public void setTourCode(String tourCode) {
        this.tourCode = tourCode;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public Destination getDestination() {
        return destination;
    }
    
    public void setDestination(Destination destination) {
        this.destination = destination;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getDurationDays() {
        return durationDays;
    }
    
    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }
    
    public Integer getMaxParticipants() {
        return maxParticipants;
    }
    
    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public String getMainImageUrl() {
        return mainImageUrl;
    }
    
    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }
    
    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
    
    public String getIncludes() {
        return includes;
    }
    
    public void setIncludes(String includes) {
        this.includes = includes;
    }
    
    public String getExcludes() {
        return excludes;
    }
    
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Boolean getFeatured() {
        return featured;
    }
    
    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<TourSchedule> getSchedules() {
        return schedules;
    }
    
    public void setSchedules(List<TourSchedule> schedules) {
        this.schedules = schedules;
    }
    
    public List<TourItinerary> getItineraries() {
        return itineraries;
    }
    
    public void setItineraries(List<TourItinerary> itineraries) {
        this.itineraries = itineraries;
    }
}
