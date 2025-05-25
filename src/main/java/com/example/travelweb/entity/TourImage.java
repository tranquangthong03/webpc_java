package com.example.travelweb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "TourImages")
public class TourImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    @NotNull
    private Tour tour;
    
    @Column(name = "image_url", nullable = false)
    @NotBlank
    private String imageUrl;
    
    @Column(name = "image_title", length = 200)
    private String imageTitle;
    
    @Column(name = "image_description", length = 500)
    private String imageDescription;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @Column(name = "is_main", nullable = false)
    private Boolean isMain = false;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public TourImage() {}
    
    public TourImage(Tour tour, String imageUrl, String imageTitle) {
        this.tour = tour;
        this.imageUrl = imageUrl;
        this.imageTitle = imageTitle;
    }
    
    // Getters and Setters
    public Long getImageId() {
        return imageId;
    }
    
    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
    
    public Tour getTour() {
        return tour;
    }
    
    public void setTour(Tour tour) {
        this.tour = tour;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getImageTitle() {
        return imageTitle;
    }
    
    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }
    
    public String getImageDescription() {
        return imageDescription;
    }
    
    public void setImageDescription(String imageDescription) {
        this.imageDescription = imageDescription;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public Boolean getIsMain() {
        return isMain;
    }
    
    public void setIsMain(Boolean isMain) {
        this.isMain = isMain;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
