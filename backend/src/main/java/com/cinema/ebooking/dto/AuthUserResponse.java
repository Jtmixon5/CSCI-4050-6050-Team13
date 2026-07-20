package com.cinema.ebooking.dto;

public record AuthUserResponse(
    Long id,
    String firstName,
    String email,
    String role
) {
}
