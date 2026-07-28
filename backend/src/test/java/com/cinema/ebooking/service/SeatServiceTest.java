package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.SeatMapResponse;
import com.cinema.ebooking.entity.Seat;
import com.cinema.ebooking.entity.Showroom;
import com.cinema.ebooking.entity.Showtime;
import com.cinema.ebooking.repository.SeatRepository;
import com.cinema.ebooking.repository.SeatReservationRepository;
import com.cinema.ebooking.repository.ShowtimeRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SeatServiceTest {

    @Test
    void seatMapMarksPersistedReservationsAsBooked() {
        ShowtimeRepository showtimeRepository = mock(ShowtimeRepository.class);
        SeatRepository seatRepository = mock(SeatRepository.class);
        SeatReservationRepository reservationRepository =
            mock(SeatReservationRepository.class);
        SeatService service = new SeatService(
            showtimeRepository,
            seatRepository,
            reservationRepository
        );

        Showtime showtime = mock(Showtime.class);
        Showroom showroom = mock(Showroom.class);
        Seat availableSeat = seat(10L, "A", 1);
        Seat bookedSeat = seat(11L, "A", 2);

        when(showtime.getId()).thenReturn(5L);
        when(showtime.getShowroom()).thenReturn(showroom);
        when(showroom.getId()).thenReturn(3L);
        when(showroom.getName()).thenReturn("Showroom 3");
        when(showtimeRepository.findById(5L)).thenReturn(Optional.of(showtime));
        when(seatRepository
            .findByShowroomIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(3L))
            .thenReturn(List.of(availableSeat, bookedSeat));
        when(reservationRepository.findReservedSeatIds(5L))
            .thenReturn(Set.of(11L));

        SeatMapResponse response = service.getSeatMap(5L);

        assertEquals("AVAILABLE", response.seats().get(0).status());
        assertEquals("BOOKED", response.seats().get(1).status());
    }

    private Seat seat(Long id, String row, int number) {
        Seat seat = mock(Seat.class);
        when(seat.getId()).thenReturn(id);
        when(seat.getRowLabel()).thenReturn(row);
        when(seat.getSeatNumber()).thenReturn(number);
        when(seat.getLabel()).thenReturn(row + number);
        when(seat.getAccessible()).thenReturn(false);
        return seat;
    }
}
