package com.cinema.ebooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cinema.ebooking.entity.Showtime;

public record ShowtimeResponse(
        Long id,
        Long movieId,
        String movieTitle,
        Long showroomId,
        String showroomName,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        BigDecimal adultPrice,
        BigDecimal childPrice,
        BigDecimal seniorPrice,
        String status
) {
    public static ShowtimeResponse from(Showtime showtime) {
        return new ShowtimeResponse(
                showtime.getId(),
                showtime.getMovie().getId(),
                showtime.getMovie().getTitle(),
                showtime.getShowroom().getId(),
                showtime.getShowroom().getName(),
                showtime.getStartsAt(),
                showtime.getEndsAt(),
                showtime.getAdultPrice(),
                showtime.getChildPrice(),
                showtime.getSeniorPrice(),
                showtime.getStatus().name()
        );
    }
}