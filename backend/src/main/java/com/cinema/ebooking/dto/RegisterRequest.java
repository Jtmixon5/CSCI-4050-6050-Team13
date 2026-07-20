package com.cinema.ebooking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "First name is required.")
    @Size(max = 100, message = "First name must be 100 characters or fewer.")
    String firstName,

    @NotBlank(message = "Last name is required.")
    @Size(max = 100, message = "Last name must be 100 characters or fewer.")
    String lastName,

    @NotBlank(message = "Email is required.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 255, message = "Email must be 255 characters or fewer.")
    String email,

    @NotBlank(message = "Phone number is required.")
    @Pattern(
        regexp = "^[0-9+() .-]{7,30}$",
        message = "Enter a valid phone number."
    )
    String phoneNumber,

    @NotBlank(message = "Password is required.")
    @Size(
        min = 8,
        max = 72,
        message = "Password must be between 8 and 72 characters."
    )
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "Password must include uppercase, lowercase, number, and special characters."
    )
    String password,

    boolean promotionOptIn
) {
}
