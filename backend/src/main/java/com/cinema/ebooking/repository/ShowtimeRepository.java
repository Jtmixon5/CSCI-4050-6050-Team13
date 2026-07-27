package com.cinema.ebooking.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cinema.ebooking.entity.Showtime;
import com.cinema.ebooking.entity.ShowtimeStatus;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieIdAndStatusAndStartsAtAfterOrderByStartsAtAsc(
            Long movieId,
            ShowtimeStatus status,
            LocalDateTime startsAt
    );

    List<Showtime> findByStatusAndStartsAtAfterOrderByStartsAtAsc(
            ShowtimeStatus status,
            LocalDateTime startsAt
    );

    boolean existsByShowroomIdAndStartsAtLessThanAndEndsAtGreaterThan(
            Long showroomId,
            LocalDateTime proposedEnd,
            LocalDateTime proposedStart
    );
}