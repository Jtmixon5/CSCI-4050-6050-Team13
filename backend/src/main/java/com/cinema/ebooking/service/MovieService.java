package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.CreateMovieRequest;
import com.cinema.ebooking.entity.Movie;
import com.cinema.ebooking.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional(readOnly = true)
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Transactional
    public Movie createMovie(CreateMovieRequest request) {
        Movie movie = new Movie(
                request.title(),
                request.category(),
                request.synopsis(),
                request.status()
        );

        return movieRepository.save(movie);
    }
}