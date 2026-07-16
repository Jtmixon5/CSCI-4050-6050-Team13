package com.cinema.ebooking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateProfileRequest(

    @NotBlank(message = "First name is required.")
    @Size(max = 100)
    String firstName,

    @NotBlank(message = "Last name is required.")
    @Size(max = 100)
    String lastName,

    @NotBlank(message = "Phone number is required.")
    @Size(max = 30)
    String phoneNumber,

    boolean promotionOptIn,

    @Valid
    AddressDto address,

    @Size(
        max = 3,
        message = "A user may store no more than three payment cards."
    )
    List<PaymentCardDto> paymentCards
) {
}
