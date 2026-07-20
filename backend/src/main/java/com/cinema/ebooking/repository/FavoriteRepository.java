package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.Favorite;
import com.cinema.ebooking.entity.FavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface FavoriteRepository
        extends JpaRepository<Favorite, FavoriteId> {

    @EntityGraph(attributePaths = "movie")
    List<Favorite> findAllByUser_IdOrderByCreatedAtDesc(
        Long userId
    );
}
