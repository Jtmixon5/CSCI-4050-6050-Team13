package com.cinema.ebooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderHistoryResponse(
    Long id,
    String confirmationNumber,
    String movieTitle,
    LocalDateTime showtime,
    String showroom,
    List<String> seats,
    long adultTickets,
    long childTickets,
    long seniorTickets,
    BigDecimal subtotal,
    BigDecimal taxAmount,
    BigDecimal totalAmount,
    String cardLastFour,
    LocalDateTime confirmedAt
) {
}
