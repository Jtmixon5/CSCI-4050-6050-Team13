package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.CreateShowtimeRequest;
import com.cinema.ebooking.entity.Movie;
import com.cinema.ebooking.entity.Showroom;
import com.cinema.ebooking.repository.MovieRepository;
import com.cinema.ebooking.repository.ShowroomRepository;
import com.cinema.ebooking.repository.ShowtimeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowtimeServiceTest {

    @Test
    void rejectsOverlappingShowtimeWithConflictStatus() {
        ShowtimeRepository showtimeRepository = mock(ShowtimeRepository.class);
        MovieRepository movieRepository = mock(MovieRepository.class);
        ShowroomRepository showroomRepository = mock(ShowroomRepository.class);
        ShowtimeService service = new ShowtimeService(
            showtimeRepository,
            movieRepository,
            showroomRepository
        );

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        CreateShowtimeRequest request = new CreateShowtimeRequest(
            1L,
            2L,
            start,
            end,
            BigDecimal.TEN,
            BigDecimal.TEN,
            BigDecimal.TEN
        );
        Movie movie = mock(Movie.class);
        Showroom showroom = mock(Showroom.class);

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(showroomRepository.findById(2L)).thenReturn(Optional.of(showroom));
        when(showroom.getId()).thenReturn(2L);
        when(showroom.getActive()).thenReturn(true);
        when(showtimeRepository
            .existsByShowroomIdAndStartsAtLessThanAndEndsAtGreaterThan(
                2L,
                end,
                start
            ))
            .thenReturn(true);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.createShowtime(request)
        );

        assertEquals(409, exception.getStatusCode().value());
    }
}
