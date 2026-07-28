package com.cinema.ebooking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.cinema.ebooking.dto.CreateShowtimeRequest;
import com.cinema.ebooking.dto.ShowtimeResponse;
import com.cinema.ebooking.entity.Movie;
import com.cinema.ebooking.entity.Showroom;
import com.cinema.ebooking.entity.Showtime;
import com.cinema.ebooking.entity.ShowtimeStatus;
import com.cinema.ebooking.repository.MovieRepository;
import com.cinema.ebooking.repository.ShowroomRepository;
import com.cinema.ebooking.repository.ShowtimeRepository;

@Service
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final ShowroomRepository showroomRepository;

    public ShowtimeService(
            ShowtimeRepository showtimeRepository,
            MovieRepository movieRepository,
            ShowroomRepository showroomRepository
    ) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.showroomRepository = showroomRepository;
    }

    @Transactional(readOnly = true)
    public List<Showroom> getActiveShowrooms() {
        return showroomRepository.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getUpcomingShowtimes(Long movieId) {
        List<Showtime> showtimes;

        if (movieId == null) {
            showtimes =
                    showtimeRepository
                            .findByStatusAndStartsAtAfterOrderByStartsAtAsc(
                                    ShowtimeStatus.SCHEDULED,
                                    LocalDateTime.now()
                            );
        } else {
            showtimes =
                    showtimeRepository
                            .findByMovieIdAndStatusAndStartsAtAfterOrderByStartsAtAsc(
                                    movieId,
                                    ShowtimeStatus.SCHEDULED,
                                    LocalDateTime.now()
                            );
        }

        return showtimes.stream()
                .map(ShowtimeResponse::from)
                .toList();
    }

    @Transactional
    public ShowtimeResponse createShowtime(CreateShowtimeRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new IllegalArgumentException(
                    "The ending time must be after the starting time."
            );
        }

        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found."));

        Showroom showroom = showroomRepository.findById(request.showroomId())
                .orElseThrow(() -> new IllegalArgumentException("Showroom not found."));

        if (!Boolean.TRUE.equals(showroom.getActive())) {
            throw new IllegalArgumentException("The selected showroom is inactive.");
        }

        boolean conflict =
                showtimeRepository
                        .existsByShowroomIdAndStartsAtLessThanAndEndsAtGreaterThan(
                                showroom.getId(),
                                request.endsAt(),
                                request.startsAt()
                        );

        if (conflict) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This showroom already has a movie scheduled during that time."
            );
        }

        Showtime showtime = new Showtime(
                movie,
                showroom,
                request.startsAt(),
                request.endsAt(),
                request.adultPrice(),
                request.childPrice(),
                request.seniorPrice()
        );

        return ShowtimeResponse.from(showtimeRepository.save(showtime));
    }
}
