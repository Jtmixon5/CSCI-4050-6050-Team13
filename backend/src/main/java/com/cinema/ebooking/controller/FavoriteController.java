package com.cinema.ebooking.controller;

import com.cinema.ebooking.entity.Movie;
import com.cinema.ebooking.service.FavoriteService;
import com.cinema.ebooking.service.AuthService;
import jakarta.servlet.http.HttpSession;
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
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final AuthService authService;

    public FavoriteController(
        FavoriteService favoriteService,
        AuthService authService
    ) {
        this.favoriteService = favoriteService;
        this.authService = authService;
    }

    @GetMapping
    public List<Movie> getFavorites(HttpSession session) {
        return favoriteService.getFavorites(userId(session));
    }

    @PostMapping("/{movieId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Movie addFavorite(
        @PathVariable Long movieId,
        HttpSession session
    ) {
        return favoriteService.addFavorite(userId(session), movieId);
    }

    @DeleteMapping("/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavorite(
        @PathVariable Long movieId,
        HttpSession session
    ) {
        favoriteService.removeFavorite(userId(session), movieId);
    }

    private Long userId(HttpSession session) {
        return authService.requireCurrentUser(session).getId();
    }
}
