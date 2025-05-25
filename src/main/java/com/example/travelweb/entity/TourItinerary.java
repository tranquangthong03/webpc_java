package com.example.travelweb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "TourItineraries", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tour_id", "day_number"}))
public class TourItinerary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itinerary_id")
    private Long itineraryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    @NotNull
    private Tour tour;
    
    @Column(name = "day_number", nullable = false)
    @NotNull
    @Min(1)
    private Integer dayNumber;
    
    @Column(name = "title", nullable = false, length = 200)
    @NotBlank
    private String title;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "activities", length = 1000)
    private String activities;
    
    @Column(name = "meals", length = 500)
    private String meals;
    
    @Column(name = "accommodation", length = 500)
    private String accommodation;
    
    // Constructors
    public TourItinerary() {}
    
    public TourItinerary(Tour tour, Integer dayNumber, String title, String description) {
        this.tour = tour;
        this.dayNumber = dayNumber;
        this.title = title;
        this.description = description;
    }
    
    // Getters and Setters
    public Long getItineraryId() {
        return itineraryId;
    }
    
    public void setItineraryId(Long itineraryId) {
        this.itineraryId = itineraryId;
    }
    
    public Tour getTour() {
        return tour;
    }
    
    public void setTour(Tour tour) {
        this.tour = tour;
    }
    
    public Integer getDayNumber() {
        return dayNumber;
    }
    
    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getActivities() {
        return activities;
    }
    
    public void setActivities(String activities) {
        this.activities = activities;
    }
    
    public String getMeals() {
        return meals;
    }
    
    public void setMeals(String meals) {
        this.meals = meals;
    }
    
    public String getAccommodation() {
        return accommodation;
    }
    
    public void setAccommodation(String accommodation) {
        this.accommodation = accommodation;
    }
}
