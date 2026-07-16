package com.cinema.ebooking.service;

import com.cinema.ebooking.entity.Favorite;
import com.cinema.ebooking.entity.FavoriteId;
import com.cinema.ebooking.entity.Movie;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.repository.FavoriteRepository;
import com.cinema.ebooking.repository.MovieRepository;
import com.cinema.ebooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public FavoriteService(
        FavoriteRepository favoriteRepository,
        UserRepository userRepository,
        MovieRepository movieRepository
    ) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
    }

    @Transactional(readOnly = true)
    public List<Movie> getFavorites(Long userId) {
        requireUser(userId);

        return favoriteRepository
            .findAllByUser_IdOrderByCreatedAtDesc(userId)
            .stream()
            .map(Favorite::getMovie)
            .toList();
    }

    @Transactional
    public Movie addFavorite(Long userId, Long movieId) {
        User user = requireUser(userId);
        Movie movie = requireMovie(movieId);

        FavoriteId favoriteId =
            new FavoriteId(userId, movieId);

        if (!favoriteRepository.existsById(favoriteId)) {
            favoriteRepository.save(
                new Favorite(user, movie)
            );
        }

        return movie;
    }

    @Transactional
    public void removeFavorite(
        Long userId,
        Long movieId
    ) {
        requireUser(userId);

        FavoriteId favoriteId =
            new FavoriteId(userId, movieId);

        if (!favoriteRepository.existsById(favoriteId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Favorite not found"
            );
        }

        favoriteRepository.deleteById(favoriteId);
    }

    private User requireUser(Long userId) {
        return userRepository
            .findById(userId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
                )
            );
    }

    private Movie requireMovie(Long movieId) {
        return movieRepository
            .findById(movieId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Movie not found"
                )
            );
    }
}
