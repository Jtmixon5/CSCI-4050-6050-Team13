package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.SeatMapResponse;
import com.cinema.ebooking.dto.SeatResponse;
import com.cinema.ebooking.entity.Seat;
import com.cinema.ebooking.entity.Showtime;
import com.cinema.ebooking.repository.SeatRepository;
import com.cinema.ebooking.repository.SeatReservationRepository;
import com.cinema.ebooking.repository.ShowtimeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class SeatService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final SeatReservationRepository seatReservationRepository;

    public SeatService(
            ShowtimeRepository showtimeRepository,
            SeatRepository seatRepository,
            SeatReservationRepository seatReservationRepository
    ) {
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.seatReservationRepository = seatReservationRepository;
    }

    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(Long showtimeId) {
        Showtime showtime = showtimeRepository
                .findById(showtimeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Showtime not found."
                        )
                );

        Long showroomId =
                showtime.getShowroom().getId();

        List<Seat> seats =
                seatRepository
                        .findByShowroomIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(
                                showroomId
                        );

        Set<Long> reservedSeatIds =
                seatReservationRepository
                        .findReservedSeatIds(showtimeId);

        List<SeatResponse> responses =
                seats.stream()
                        .map(seat ->
                                SeatResponse.from(
                                        seat,
                                        reservedSeatIds.contains(
                                                seat.getId()
                                        )
                                )
                        )
                        .toList();

        return new SeatMapResponse(
                showtime.getId(),
                showroomId,
                showtime.getShowroom().getName(),
                responses
        );
    }
}
