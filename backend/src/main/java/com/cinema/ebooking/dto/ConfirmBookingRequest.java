package com.cinema.ebooking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record ConfirmBookingRequest(
    Long savedCardId,

    @Pattern(regexp = "\\d{13,19}", message = "Card number must contain 13 to 19 digits.")
    String cardNumber,
    @Min(1) @Max(12) Integer expirationMonth,
    @Min(2024) Integer expirationYear,

    @Pattern(regexp = "\\d{3,4}", message = "Security code must contain 3 or 4 digits.")
    String securityCode
) {
}
