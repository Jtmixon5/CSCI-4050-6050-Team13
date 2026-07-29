package com.cinema.ebooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "session_token", nullable = false, unique = true, length = 128)
    private String sessionToken;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "confirmation_number", unique = true, length = 40)
    private String confirmationNumber;

    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    protected Booking() {
    }

    public Booking(
        Showtime showtime,
        String sessionToken,
        BigDecimal subtotal,
        LocalDateTime expiresAt
    ) {
        this.showtime = showtime;
        this.sessionToken = sessionToken;
        this.status = BookingStatus.DRAFT;
        this.subtotal = subtotal;
        this.totalAmount = subtotal;
        this.expiresAt = expiresAt;
    }

    public void updateDraft(
        Showtime showtime,
        BigDecimal subtotal,
        LocalDateTime expiresAt
    ) {
        this.showtime = showtime;
        this.subtotal = subtotal;
        this.taxAmount = BigDecimal.ZERO;
        this.totalAmount = subtotal;
        this.expiresAt = expiresAt;
        this.status = BookingStatus.DRAFT;
    }

    public void proceedToPayment(User user, String contactEmail) {
        this.user = user;
        this.contactEmail = contactEmail;
        this.status = BookingStatus.PAYMENT_PENDING;
    }

    public void confirm(
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String confirmationNumber,
        String cardLastFour
    ) {
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.confirmationNumber = confirmationNumber;
        this.cardLastFour = cardLastFour;
        this.confirmedAt = LocalDateTime.now();
        this.expiresAt = null;
        this.status = BookingStatus.CONFIRMED;
    }

    public Long getId() {
        return id;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public User getUser() {
        return user;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getConfirmationNumber() { return confirmationNumber; }
    public String getCardLastFour() { return cardLastFour; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
}
