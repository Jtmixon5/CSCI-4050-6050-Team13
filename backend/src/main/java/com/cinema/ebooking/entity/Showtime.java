package com.cinema.ebooking.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

@Entity
@Table(name = "showtimes")
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "showroom_id", nullable = false)
    private Showroom showroom;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    @Column(name = "adult_price", nullable = false)
    private BigDecimal adultPrice;

    @Column(name = "child_price", nullable = false)
    private BigDecimal childPrice;

    @Column(name = "senior_price", nullable = false)
    private BigDecimal seniorPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShowtimeStatus status;

    protected Showtime() {
    }

    public Showtime(
            Movie movie,
            Showroom showroom,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            BigDecimal adultPrice,
            BigDecimal childPrice,
            BigDecimal seniorPrice
    ) {
        this.movie = movie;
        this.showroom = showroom;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.adultPrice = adultPrice;
        this.childPrice = childPrice;
        this.seniorPrice = seniorPrice;
        this.status = ShowtimeStatus.SCHEDULED;
    }

    public Long getId() {
        return id;
    }

    public Movie getMovie() {
        return movie;
    }

    public Showroom getShowroom() {
        return showroom;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public BigDecimal getAdultPrice() {
        return adultPrice;
    }

    public BigDecimal getChildPrice() {
        return childPrice;
    }

    public BigDecimal getSeniorPrice() {
        return seniorPrice;
    }

    public ShowtimeStatus getStatus() {
        return status;
    }
}