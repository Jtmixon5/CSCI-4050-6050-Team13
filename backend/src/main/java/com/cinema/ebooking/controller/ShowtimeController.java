package com.cinema.ebooking.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.ebooking.dto.CreateShowtimeRequest;
import com.cinema.ebooking.dto.ShowtimeResponse;
import com.cinema.ebooking.entity.Showroom;
import com.cinema.ebooking.service.ShowtimeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping("/showrooms")
    public List<Showroom> getShowrooms() {
        return showtimeService.getActiveShowrooms();
    }

    @GetMapping("/showtimes")
    public List<ShowtimeResponse> getShowtimes(
            @RequestParam(required = false) Long movieId
    ) {
        return showtimeService.getUpcomingShowtimes(movieId);
    }

    @PostMapping("/admin/showtimes")
    public ResponseEntity<ShowtimeResponse> createShowtime(
            @Valid @RequestBody CreateShowtimeRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(showtimeService.createShowtime(request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRequest(
            IllegalArgumentException exception
    ) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", exception.getMessage()));
    }
}