package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import com.cinema.ebooking.entity.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findBySessionToken(String sessionToken);
    List<Booking> findAllByUser_IdAndStatusOrderByConfirmedAtDesc(
        Long userId,
        BookingStatus status
    );
}
