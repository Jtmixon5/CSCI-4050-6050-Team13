package com.cinema.ebooking.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.cinema.ebooking.entity.Seat;

public interface SeatReservationRepository
        extends Repository<Seat, Long> {

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
            """,
        nativeQuery = true
    )
    Set<Long> findReservedSeatIds(
            @Param("showtimeId") Long showtimeId
    );
}
