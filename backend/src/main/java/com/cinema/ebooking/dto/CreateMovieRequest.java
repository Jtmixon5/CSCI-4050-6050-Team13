package com.cinema.ebooking.dto;

import com.cinema.ebooking.entity.MovieStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMovieRequest(
        @NotBlank String title,
        @NotBlank String category,
        String synopsis,
        @NotNull MovieStatus status
) {
}