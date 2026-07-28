package com.cinema.ebooking.dto;

import com.cinema.ebooking.entity.Seat;

public record SeatResponse(
        Long id,
        String label,
        String rowLabel,
        Integer seatNumber,
        boolean accessible,
        String status
) {
    public static SeatResponse from(
            Seat seat,
            boolean booked
    ) {
        return new SeatResponse(
                seat.getId(),
                seat.getLabel(),
                seat.getRowLabel(),
                seat.getSeatNumber(),
                Boolean.TRUE.equals(seat.getAccessible()),
                booked ? "BOOKED" : "AVAILABLE"
        );
    }
}
