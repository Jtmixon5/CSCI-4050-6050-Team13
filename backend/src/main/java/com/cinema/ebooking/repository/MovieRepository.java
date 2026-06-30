package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByTitleContainingIgnoreCase(String title);

    List<Movie> findByCategoryIgnoreCase(String category);

    List<Movie> findByTitleContainingIgnoreCaseAndCategoryIgnoreCase(
            String title,
            String category
    );
}