package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cinema.ebooking.entity.MovieStatus;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

List<Movie> findByStatus(MovieStatus status);

List<Movie> findByStatusAndTitleContainingIgnoreCase(MovieStatus status, String title);

List<Movie> findByStatusAndCategoryIgnoreCase(MovieStatus status, String category);

List<Movie> findByStatusAndTitleContainingIgnoreCaseAndCategoryIgnoreCase(
        MovieStatus status,
        String title,
        String category
);
}
