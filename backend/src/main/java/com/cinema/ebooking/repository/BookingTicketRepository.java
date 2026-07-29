package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.BookingTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingTicketRepository
    extends JpaRepository<BookingTicket, Long> {

    void deleteByBookingId(Long bookingId);
    List<BookingTicket> findByBooking_Id(Long bookingId);
}
