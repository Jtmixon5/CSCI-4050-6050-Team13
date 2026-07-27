package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.CreateMovieRequest;
import com.cinema.ebooking.entity.Movie;
import com.cinema.ebooking.entity.MovieStatus;
import com.cinema.ebooking.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Test
    void createMovieStoresAllSubmittedMetadata() {
        MovieService service = new MovieService(movieRepository);
        CreateMovieRequest request = new CreateMovieRequest(
            "  Test Movie  ",
            "  Drama  ",
            "  A test synopsis.  ",
            "  Actor One, Actor Two  ",
            "  Test Director  ",
            "  Test Producer  ",
            "  PG-13  ",
            "  https://example.com/poster.jpg  ",
            "  https://example.com/trailer  ",
            MovieStatus.COMING_SOON
        );
        when(movieRepository.save(org.mockito.ArgumentMatchers.any(Movie.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Movie saved = service.createMovie(request);

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(movieCaptor.capture());
        Movie persisted = movieCaptor.getValue();
        assertEquals(saved, persisted);
        assertEquals("Test Movie", persisted.getTitle());
        assertEquals("Drama", persisted.getCategory());
        assertEquals("A test synopsis.", persisted.getSynopsis());
        assertEquals("Actor One, Actor Two", persisted.getCastMembers());
        assertEquals("Test Director", persisted.getDirector());
        assertEquals("Test Producer", persisted.getProducer());
        assertEquals("PG-13", persisted.getMpaaRating());
        assertEquals("https://example.com/poster.jpg", persisted.getPosterUrl());
        assertEquals("https://example.com/trailer", persisted.getTrailerUrl());
        assertEquals(MovieStatus.COMING_SOON, persisted.getStatus());
    }
}
