package com.cinema.ebooking.dto;

import com.cinema.ebooking.entity.MovieStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateMovieRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 100) String category,
        @NotBlank @Size(max = 5000) String synopsis,
        @NotBlank @Size(max = 5000) String castMembers,
        @NotBlank @Size(max = 200) String director,
        @NotBlank @Size(max = 200) String producer,
        @NotBlank @Size(max = 20) String mpaaRating,
        @NotBlank @Size(max = 500)
        @Pattern(regexp = "https?://.+", message = "must be an HTTP or HTTPS URL")
        String posterUrl,
        @NotBlank @Size(max = 500)
        @Pattern(regexp = "https?://.+", message = "must be an HTTP or HTTPS URL")
        String trailerUrl,
        @NotNull MovieStatus status
) {
}
