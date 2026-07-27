package com.cinema.ebooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateShowtimeRequest(

        @NotNull
        @Positive
        Long movieId,

        @NotNull
        @Positive
        Long showroomId,

        @NotNull
        @Future
        LocalDateTime startsAt,

        @NotNull
        @Future
        LocalDateTime endsAt,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal adultPrice,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal childPrice,

        @NotNull
        @DecimalMin(value = "0.00")
        BigDecimal seniorPrice
) {
}