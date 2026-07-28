package com.cinema.ebooking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record ReserveSeatsRequest(
    @NotNull @Positive Long showtimeId,
    @NotNull @NotEmpty List<@NotNull @Positive Long> seatIds,
    @PositiveOrZero int adultTickets,
    @PositiveOrZero int childTickets,
    @PositiveOrZero int seniorTickets
) {
}
