package com.cinema.ebooking.dto;

import java.util.List;

public record UserProfileResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    boolean promotionOptIn,
    AddressDto address,
    List<PaymentCardDto> paymentCards
) {
}
