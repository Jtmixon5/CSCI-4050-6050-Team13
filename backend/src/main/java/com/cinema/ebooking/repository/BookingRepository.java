package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findBySessionToken(String sessionToken);
}
