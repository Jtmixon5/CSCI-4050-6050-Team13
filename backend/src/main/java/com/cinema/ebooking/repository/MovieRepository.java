package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.Movie;
import com.cinema.ebooking.entity.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByTitleContainingIgnoreCase(String title);

    List<Movie> findByCategoryIgnoreCase(String category);

    List<Movie> findByTitleContainingIgnoreCaseAndCategoryIgnoreCase(
            String title,
            String category
    );

    List<Movie> findByStatus(MovieStatus status);

    List<Movie> findByStatusAndTitleContainingIgnoreCase(
            MovieStatus status,
            String title
    );

    List<Movie> findByStatusAndCategoryIgnoreCase(
            MovieStatus status,
            String category
    );

    List<Movie> findByStatusAndTitleContainingIgnoreCaseAndCategoryIgnoreCase(
            MovieStatus status,
            String title,
            String category
    );
}
