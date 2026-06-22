package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.Movie;
import com.cinema.ebooking.entity.MovieStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatus(MovieStatus status);

    List<Movie> findByTitleContainingIgnoreCase(String title);

    List<Movie> findByCategoryIgnoreCase(String category);
}