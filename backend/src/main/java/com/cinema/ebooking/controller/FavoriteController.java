package com.cinema.ebooking.controller;

import com.cinema.ebooking.entity.Movie;
import com.cinema.ebooking.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public List<Movie> getFavorites(@PathVariable Long userId) {
        return favoriteService.getFavorites(userId);
    }

    @PostMapping("/{movieId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Movie addFavorite(
        @PathVariable Long userId,
        @PathVariable Long movieId
    ) {
        return favoriteService.addFavorite(userId, movieId);
    }

    @DeleteMapping("/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(
        @PathVariable Long userId,
        @PathVariable Long movieId
    ) {
        favoriteService.removeFavorite(userId, movieId);
    }
}
