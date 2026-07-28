package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SeatReservationRepository
    extends JpaRepository<SeatReservation, Long> {

    @Query(
        value = """
            SELECT sr.seat_id
            FROM seat_reservations sr
            JOIN bookings b ON b.id = sr.booking_id
            WHERE sr.showtime_id = :showtimeId
              AND b.status IN (
                  'DRAFT',
                  'CHECKOUT',
                  'PAYMENT_PENDING',
                  'CONFIRMED'
              )
              AND (
                  b.status = 'CONFIRMED'
                  OR sr.expires_at IS NULL
                  OR sr.expires_at > CURRENT_TIMESTAMP
              )
            """,
        nativeQuery = true
    )
    Set<Long> findReservedSeatIds(
        @Param("showtimeId") Long showtimeId
    );

    List<SeatReservation> findByBooking_IdOrderBySeat_RowLabelAscSeat_SeatNumberAsc(
        Long bookingId
    );

    @Modifying
    @Query("delete from SeatReservation reservation where reservation.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}
