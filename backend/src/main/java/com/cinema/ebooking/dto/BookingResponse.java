package com.cinema.ebooking.dto;

import com.cinema.ebooking.entity.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
    Long id,
    Long showtimeId,
    String status,
    BigDecimal subtotal,
    String contactEmail,
    LocalDateTime expiresAt,
    List<Long> seatIds
) {
    public static BookingResponse from(Booking booking, List<Long> seatIds) {
        return new BookingResponse(
            booking.getId(),
            booking.getShowtime().getId(),
            booking.getStatus().name(),
            booking.getSubtotal(),
            booking.getContactEmail(),
            booking.getExpiresAt(),
            seatIds
        );
    }
}
