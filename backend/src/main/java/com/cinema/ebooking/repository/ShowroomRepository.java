package com.cinema.ebooking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cinema.ebooking.entity.Showroom;

public interface ShowroomRepository extends JpaRepository<Showroom, Long> {

    List<Showroom> findByActiveTrueOrderByNameAsc();
}