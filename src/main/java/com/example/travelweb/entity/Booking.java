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
@Table(name = "Bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;
    
    @Column(name = "booking_code", nullable = false, unique = true, length = 20)
    @NotBlank
    private String bookingCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    @NotNull
    private TourSchedule schedule;
    
    @Column(name = "adult_count", nullable = false)
    @Min(1)
    private Integer adultCount;
    
    @Column(name = "child_count", nullable = false)
    @Min(0)
    private Integer childCount = 0;
    
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal totalAmount;
    
    @Column(name = "discount_amount", precision = 18, scale = 2)
    @DecimalMin("0.0")
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "final_amount", nullable = false, precision = 18, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal finalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 20)
    private BookingStatus bookingStatus = BookingStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    
    @Column(name = "customer_notes", length = 1000)
    private String customerNotes;
    
    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;
    
    @CreationTimestamp
    @Column(name = "booking_date")
    private LocalDateTime bookingDate;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancelled_reason", length = 500)
    private String cancelledReason;
    
    // Quan hệ One-to-Many
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingParticipant> participants = new ArrayList<>();
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();
    
    // Enums
    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, COMPLETED
    }
    
    public enum PaymentStatus {
        UNPAID, PARTIAL, PAID, REFUNDED
    }
    
    // Enum Status cho AdminController
    public enum Status {
        PENDING, CONFIRMED, CANCELLED, COMPLETED;
        
        public BookingStatus toBookingStatus() {
            return BookingStatus.valueOf(this.name());
        }
    }
    
    // Constructors
    public Booking() {}
    
    public Booking(String bookingCode, User user, TourSchedule schedule, 
                   Integer adultCount, Integer childCount, BigDecimal totalAmount) {
        this.bookingCode = bookingCode;
        this.user = user;
        this.schedule = schedule;
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.totalAmount = totalAmount;
        this.finalAmount = totalAmount;
    }
    
    // Business methods
    public Integer getTotalParticipants() {
        return adultCount + childCount;
    }
    
    public BigDecimal getDiscountedAmount() {
        return totalAmount.subtract(discountAmount);
    }
    
    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getBookingCode() {
        return bookingCode;
    }
    
    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public TourSchedule getSchedule() {
        return schedule;
    }
    
    public void setSchedule(TourSchedule schedule) {
        this.schedule = schedule;
    }
    
    public Integer getAdultCount() {
        return adultCount;
    }
    
    public void setAdultCount(Integer adultCount) {
        this.adultCount = adultCount;
    }
    
    public Integer getChildCount() {
        return childCount;
    }
    
    public void setChildCount(Integer childCount) {
        this.childCount = childCount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getFinalAmount() {
        return finalAmount;
    }
    
    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }
    
    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }
    
    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getCustomerNotes() {
        return customerNotes;
    }
    
    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }
    
    public String getAdminNotes() {
        return adminNotes;
    }
    
    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
    
    public LocalDateTime getBookingDate() {
        return bookingDate;
    }
    
    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }
    
    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
    
    public String getCancelledReason() {
        return cancelledReason;
    }
    
    public void setCancelledReason(String cancelledReason) {
        this.cancelledReason = cancelledReason;
    }
    
    public List<BookingParticipant> getParticipants() {
        return participants;
    }
    
    public void setParticipants(List<BookingParticipant> participants) {
        this.participants = participants;
    }
    
    public List<Payment> getPayments() {
        return payments;
    }
    
    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
}
