package com.cinema.ebooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "Current password is required.")
    String currentPassword,

    @NotBlank(message = "New password is required.")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "Password must include uppercase, lowercase, number, and special characters."
    )
    String newPassword
) {
}
