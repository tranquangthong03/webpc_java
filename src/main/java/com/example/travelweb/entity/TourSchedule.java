package com.example.travelweb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TourSchedules")
public class TourSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    @NotNull
    private Tour tour;
    
    @Column(name = "departure_date", nullable = false)
    @NotNull
    @Future
    private LocalDate departureDate;
    
    @Column(name = "return_date", nullable = false)
    @NotNull
    private LocalDate returnDate;
    
    @Column(name = "available_slots", nullable = false)
    @Min(1)
    private Integer availableSlots;
    
    @Column(name = "booked_slots", nullable = false)
    @Min(0)
    private Integer bookedSlots = 0;
    
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.AVAILABLE;
    
    @Column(name = "guide_name", length = 100)
    private String guideName;
    
    @Column(name = "guide_phone", length = 15)
    private String guidePhone;
    
    @Column(name = "meeting_point", length = 500)
    private String meetingPoint;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Quan hệ One-to-Many với Booking
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();
    
    // Enums
    public enum Status {
        AVAILABLE, FULL, CANCELLED, COMPLETED
    }
    
    // Constructors
    public TourSchedule() {}
    
    public TourSchedule(Tour tour, LocalDate departureDate, LocalDate returnDate, 
                       Integer availableSlots, BigDecimal price) {
        this.tour = tour;
        this.departureDate = departureDate;
        this.returnDate = returnDate;
        this.availableSlots = availableSlots;
        this.price = price;
    }
    
    // Business methods
    public Integer getAvailableSpace() {
        return availableSlots - bookedSlots;
    }
    
    public boolean hasAvailableSpace() {
        return getAvailableSpace() > 0;
    }
    
    public boolean hasAvailableSpace(int requestedSlots) {
        return getAvailableSpace() >= requestedSlots;
    }
    
    // Getters and Setters
    public Long getScheduleId() {
        return scheduleId;
    }
    
    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }
    
    public Tour getTour() {
        return tour;
    }
    
    public void setTour(Tour tour) {
        this.tour = tour;
    }
    
    public LocalDate getDepartureDate() {
        return departureDate;
    }
    
    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }
    
    public LocalDate getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    
    public Integer getAvailableSlots() {
        return availableSlots;
    }
    
    public void setAvailableSlots(Integer availableSlots) {
        this.availableSlots = availableSlots;
    }
    
    public Integer getBookedSlots() {
        return bookedSlots;
    }
    
    public void setBookedSlots(Integer bookedSlots) {
        this.bookedSlots = bookedSlots;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getGuideName() {
        return guideName;
    }
    
    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }
    
    public String getGuidePhone() {
        return guidePhone;
    }
    
    public void setGuidePhone(String guidePhone) {
        this.guidePhone = guidePhone;
    }
    
    public String getMeetingPoint() {
        return meetingPoint;
    }
    
    public void setMeetingPoint(String meetingPoint) {
        this.meetingPoint = meetingPoint;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<Booking> getBookings() {
        return bookings;
    }
    
    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}
