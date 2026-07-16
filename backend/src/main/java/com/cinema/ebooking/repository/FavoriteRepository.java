package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findAllByUserIdOrderByIdDesc(Long userId);

    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    Optional<Favorite> findByUserIdAndMovieId(Long userId, Long movieId);
}
