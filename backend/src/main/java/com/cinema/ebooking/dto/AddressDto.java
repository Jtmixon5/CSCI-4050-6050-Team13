package com.cinema.ebooking.dto;

public record AddressDto(
    String street,
    String city,
    String state,
    String zipCode
) {
}
