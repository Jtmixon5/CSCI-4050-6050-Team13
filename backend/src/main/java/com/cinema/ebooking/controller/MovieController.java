package com.cinema.ebooking.controller;

import com.cinema.ebooking.dto.CreateMovieRequest;
import com.cinema.ebooking.entity.Movie;
import com.cinema.ebooking.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movie createMovie(
            @Valid @RequestBody CreateMovieRequest request
    ) {
        return movieService.createMovie(request);
    }
}