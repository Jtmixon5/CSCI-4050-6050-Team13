package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.CreateMovieRequest;
import com.cinema.ebooking.entity.Movie;
import com.cinema.ebooking.entity.MovieStatus;
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
public List<Movie> searchMovies(String title, String category, MovieStatus status) {
    boolean hasTitle = title != null && !title.isBlank();
    boolean hasCategory = category != null && !category.isBlank();
    boolean hasStatus = status != null;

    if (hasStatus && hasTitle && hasCategory) {
        return movieRepository.findByStatusAndTitleContainingIgnoreCaseAndCategoryIgnoreCase(
                status,
                title,
                category
        );
    }

    if (hasStatus && hasTitle) {
        return movieRepository.findByStatusAndTitleContainingIgnoreCase(status, title);
    }

    if (hasStatus && hasCategory) {
        return movieRepository.findByStatusAndCategoryIgnoreCase(status, category);
    }

    if (hasStatus) {
        return movieRepository.findByStatus(status);
    }

    if (hasTitle && hasCategory) {
        return movieRepository.findByTitleContainingIgnoreCaseAndCategoryIgnoreCase(
                title,
                category
        );
    }

    if (hasTitle) {
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    if (hasCategory) {
        return movieRepository.findByCategoryIgnoreCase(category);
    }

    return movieRepository.findAll();
}

    @Transactional(readOnly = true)
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
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
