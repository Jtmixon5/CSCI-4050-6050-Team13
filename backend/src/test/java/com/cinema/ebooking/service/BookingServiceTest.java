package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.BookingResponse;
import com.cinema.ebooking.dto.ReserveSeatsRequest;
import com.cinema.ebooking.entity.Booking;
import com.cinema.ebooking.entity.BookingTicket;
import com.cinema.ebooking.entity.Seat;
import com.cinema.ebooking.entity.SeatReservation;
import com.cinema.ebooking.entity.Showroom;
import com.cinema.ebooking.entity.Showtime;
import com.cinema.ebooking.entity.ShowtimeStatus;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.repository.BookingRepository;
import com.cinema.ebooking.repository.BookingTicketRepository;
import com.cinema.ebooking.repository.SeatRepository;
import com.cinema.ebooking.repository.SeatReservationRepository;
import com.cinema.ebooking.repository.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private BookingTicketRepository ticketRepository;
    private SeatReservationRepository reservationRepository;
    private ShowtimeRepository showtimeRepository;
    private SeatRepository seatRepository;
    private BookingService service;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        ticketRepository = mock(BookingTicketRepository.class);
        reservationRepository = mock(SeatReservationRepository.class);
        showtimeRepository = mock(ShowtimeRepository.class);
        seatRepository = mock(SeatRepository.class);
        service = new BookingService(
            bookingRepository,
            ticketRepository,
            reservationRepository,
            showtimeRepository,
            seatRepository
        );
    }

    @Test
    void rejectsWhenTicketAndSeatCountsDoNotMatch() {
        ReserveSeatsRequest request = new ReserveSeatsRequest(
            1L,
            List.of(10L),
            2,
            0,
            0
        );

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.reserve(request, "session-token")
        );

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void createsTicketsAndReservationsWithServerCalculatedSubtotal() {
        Showtime showtime = showtime();
        List<Seat> seats = List.of(seat(10L, showtime), seat(11L, showtime));
        ReserveSeatsRequest request = new ReserveSeatsRequest(
            1L,
            List.of(10L, 11L),
            1,
            1,
            0
        );

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findAllById(request.seatIds())).thenReturn(seats);
        when(bookingRepository.findBySessionToken("session-token"))
            .thenReturn(Optional.empty());
        when(bookingRepository.saveAndFlush(any(Booking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.saveAllAndFlush(anyList()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = service.reserve(request, "session-token");

        assertEquals(new BigDecimal("24.00"), response.subtotal());
        assertEquals(List.of(10L, 11L), response.seatIds());
        verify(ticketRepository).saveAllAndFlush(anyList());
        verify(reservationRepository).saveAll(anyList());
        verify(reservationRepository).flush();
    }

    @Test
    void translatesDatabaseSeatConflictToHttpConflict() {
        Showtime showtime = showtime();
        ReserveSeatsRequest request = new ReserveSeatsRequest(
            1L,
            List.of(10L),
            1,
            0,
            0
        );
        Seat selectedSeat = seat(10L, showtime);

        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findAllById(request.seatIds()))
            .thenReturn(List.of(selectedSeat));
        when(bookingRepository.findBySessionToken("session-token"))
            .thenReturn(Optional.empty());
        when(bookingRepository.saveAndFlush(any(Booking.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.saveAllAndFlush(anyList()))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(reservationRepository.saveAll(anyList()))
            .thenThrow(new DataIntegrityViolationException("duplicate seat"));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.reserve(request, "session-token")
        );

        assertEquals(409, exception.getStatusCode().value());
    }

    @Test
    void checkoutAttachesAuthenticatedUserAndContactEmail() {
        Showtime showtime = showtime();
        Booking booking = new Booking(
            showtime,
            "session-token",
            new BigDecimal("14.00"),
            LocalDateTime.now().plusMinutes(5)
        );
        User user = mock(User.class);
        Seat seat = seat(10L, showtime);
        SeatReservation reservation = mock(SeatReservation.class);
        when(reservation.getSeat()).thenReturn(seat);
        when(bookingRepository.findBySessionToken("session-token"))
            .thenReturn(Optional.of(booking));
        when(reservationRepository
            .findByBooking_IdOrderBySeat_RowLabelAscSeat_SeatNumberAsc(null))
            .thenReturn(List.of(reservation));

        BookingResponse response = service.checkout(
            "session-token",
            user,
            "  customer@example.com  "
        );

        assertEquals("PAYMENT_PENDING", response.status());
        assertEquals("customer@example.com", response.contactEmail());
        assertEquals(List.of(10L), response.seatIds());
        verify(bookingRepository).save(booking);
    }

    private Showtime showtime() {
        Showtime showtime = mock(Showtime.class);
        Showroom showroom = mock(Showroom.class);
        when(showroom.getId()).thenReturn(3L);
        when(showtime.getId()).thenReturn(1L);
        when(showtime.getShowroom()).thenReturn(showroom);
        when(showtime.getStatus()).thenReturn(ShowtimeStatus.SCHEDULED);
        when(showtime.getStartsAt()).thenReturn(LocalDateTime.now().plusDays(1));
        when(showtime.getAdultPrice()).thenReturn(new BigDecimal("14.00"));
        when(showtime.getChildPrice()).thenReturn(new BigDecimal("10.00"));
        when(showtime.getSeniorPrice()).thenReturn(new BigDecimal("11.00"));
        return showtime;
    }

    private Seat seat(Long id, Showtime showtime) {
        Seat seat = mock(Seat.class);
        Showroom showroom = showtime.getShowroom();
        when(seat.getId()).thenReturn(id);
        when(seat.getShowroom()).thenReturn(showroom);
        when(seat.getActive()).thenReturn(true);
        return seat;
    }
}
