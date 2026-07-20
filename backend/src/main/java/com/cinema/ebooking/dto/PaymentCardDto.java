package com.cinema.ebooking.dto;

public record PaymentCardDto(
    Long id,
    String cardholderName,
    String cardType,
    String cardNumber,
    String expirationMonth,
    String expirationYear,
    String lastFour,
    String billingZipCode
) {
}
