package com.cinema.ebooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_reservations")
public class SeatReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(name = "showroom_id", nullable = false)
    private Long showroomId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private BookingTicket ticket;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    protected SeatReservation() {
    }

    public SeatReservation(
        Booking booking,
        Showtime showtime,
        Seat seat,
        BookingTicket ticket,
        LocalDateTime expiresAt
    ) {
        this.booking = booking;
        this.showtime = showtime;
        this.showroomId = showtime.getShowroom().getId();
        this.seat = seat;
        this.ticket = ticket;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public Long getShowroomId() {
        return showroomId;
    }

    public Seat getSeat() {
        return seat;
    }

    public BookingTicket getTicket() {
        return ticket;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
