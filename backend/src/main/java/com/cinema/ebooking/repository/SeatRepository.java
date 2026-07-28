package com.cinema.ebooking.repository;

import com.cinema.ebooking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowroomIdAndActiveTrueOrderByRowLabelAscSeatNumberAsc(
            Long showroomId
    );
}
