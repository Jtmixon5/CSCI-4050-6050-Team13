package com.cinema.ebooking.dto;

public record PaymentCardDto(
    Long id,
    String cardholderName,
    String lastFour,
    String billingZipCode
) {
}
