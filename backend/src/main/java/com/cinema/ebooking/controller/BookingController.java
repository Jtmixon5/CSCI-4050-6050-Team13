package com.cinema.ebooking.controller;

import com.cinema.ebooking.dto.BookingResponse;
import com.cinema.ebooking.dto.CheckoutBookingRequest;
import com.cinema.ebooking.dto.ConfirmBookingRequest;
import com.cinema.ebooking.dto.OrderHistoryResponse;
import com.cinema.ebooking.dto.ReserveSeatsRequest;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.service.AuthService;
import com.cinema.ebooking.service.BookingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final AuthService authService;

    public BookingController(
        BookingService bookingService,
        AuthService authService
    ) {
        this.bookingService = bookingService;
        this.authService = authService;
    }

    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse reserve(
        @Valid @RequestBody ReserveSeatsRequest request,
        HttpSession session
    ) {
        String token = (String) session.getAttribute(
            BookingService.BOOKING_SESSION_TOKEN
        );
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute(BookingService.BOOKING_SESSION_TOKEN, token);
        }
        return bookingService.reserve(request, token);
    }

    @PostMapping("/checkout")
    public BookingResponse checkout(
        @Valid @RequestBody CheckoutBookingRequest request,
        HttpSession session
    ) {
        User user = authService.requireCurrentUser(session);
        String token = (String) session.getAttribute(
            BookingService.BOOKING_SESSION_TOKEN
        );
        if (token == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No active booking was found for this session."
            );
        }
        return bookingService.checkout(token, user, request.contactEmail());
    }

    @PostMapping("/confirm")
    public BookingResponse confirm(
        @Valid @RequestBody ConfirmBookingRequest request,
        HttpSession session
    ) {
        User user = authService.requireCurrentUser(session);
        String token = requireBookingToken(session);
        BookingResponse response = bookingService.confirm(token, user, request);
        session.removeAttribute(BookingService.BOOKING_SESSION_TOKEN);
        return response;
    }

    @GetMapping("/history")
    public List<OrderHistoryResponse> history(HttpSession session) {
        return bookingService.history(authService.requireCurrentUser(session));
    }

    private String requireBookingToken(HttpSession session) {
        String token = (String) session.getAttribute(
            BookingService.BOOKING_SESSION_TOKEN
        );
        if (token == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No active booking was found for this session."
            );
        }
        return token;
    }
}
