package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.BookingTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingTicketRepository
    extends JpaRepository<BookingTicket, Long> {

    void deleteByBookingId(Long bookingId);
}
