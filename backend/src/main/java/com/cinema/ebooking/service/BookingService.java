package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.BookingResponse;
import com.cinema.ebooking.dto.ConfirmBookingRequest;
import com.cinema.ebooking.dto.OrderHistoryResponse;
import com.cinema.ebooking.dto.ReserveSeatsRequest;
import com.cinema.ebooking.entity.Booking;
import com.cinema.ebooking.entity.BookingStatus;
import com.cinema.ebooking.entity.BookingTicket;
import com.cinema.ebooking.entity.Seat;
import com.cinema.ebooking.entity.SeatReservation;
import com.cinema.ebooking.entity.Showtime;
import com.cinema.ebooking.entity.ShowtimeStatus;
import com.cinema.ebooking.entity.TicketType;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.repository.BookingRepository;
import com.cinema.ebooking.repository.BookingTicketRepository;
import com.cinema.ebooking.repository.SeatRepository;
import com.cinema.ebooking.repository.SeatReservationRepository;
import com.cinema.ebooking.repository.ShowtimeRepository;
import com.cinema.ebooking.payment.PaymentGateway;
import com.cinema.ebooking.payment.PaymentRejectedException;
import com.cinema.ebooking.pattern.ConfirmationNumberGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    public static final String BOOKING_SESSION_TOKEN = "bookingSessionToken";
    private static final int HOLD_MINUTES = 5;
    private static final BigDecimal TAX_RATE = new BigDecimal("0.07");

    private final BookingRepository bookingRepository;
    private final BookingTicketRepository ticketRepository;
    private final SeatReservationRepository reservationRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final PaymentGateway paymentGateway;
    private final RegistrationEmailService emailService;
    private final CheckoutPaymentCardService checkoutCardService;

    public BookingService(
        BookingRepository bookingRepository,
        BookingTicketRepository ticketRepository,
        SeatReservationRepository reservationRepository,
        ShowtimeRepository showtimeRepository,
        SeatRepository seatRepository,
        PaymentGateway paymentGateway,
        RegistrationEmailService emailService,
        CheckoutPaymentCardService checkoutCardService
    ) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.reservationRepository = reservationRepository;
        this.showtimeRepository = showtimeRepository;
        this.seatRepository = seatRepository;
        this.paymentGateway = paymentGateway;
        this.emailService = emailService;
        this.checkoutCardService = checkoutCardService;
    }

    @Transactional
    public BookingResponse reserve(
        ReserveSeatsRequest request,
        String sessionToken
    ) {
        int ticketCount =
            request.adultTickets()
                + request.childTickets()
                + request.seniorTickets();

        if (ticketCount < 1 || ticketCount != request.seatIds().size()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The selected seat count must match the ticket count."
            );
        }

        if (new HashSet<>(request.seatIds()).size() != request.seatIds().size()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A seat may only be selected once."
            );
        }

        Showtime showtime = showtimeRepository.findById(request.showtimeId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Showtime not found."
            ));

        if (showtime.getStatus() != ShowtimeStatus.SCHEDULED
            || !showtime.getStartsAt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This showtime is no longer available for booking."
            );
        }

        List<Seat> seats = seatRepository.findAllById(request.seatIds());
        if (seats.size() != request.seatIds().size()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "One or more selected seats do not exist."
            );
        }

        Long showroomId = showtime.getShowroom().getId();
        boolean invalidSeat = seats.stream().anyMatch(seat ->
            !showroomId.equals(seat.getShowroom().getId())
                || !Boolean.TRUE.equals(seat.getActive())
        );
        if (invalidSeat) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Every selected seat must belong to the showtime's active showroom."
            );
        }

        List<TicketType> ticketTypes = ticketTypes(request);
        BigDecimal subtotal = calculateSubtotal(showtime, request);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(HOLD_MINUTES);

        Booking booking = bookingRepository.findBySessionToken(sessionToken)
            .orElseGet(() -> new Booking(
                showtime,
                sessionToken,
                subtotal,
                expiresAt
            ));

        if (booking.getId() != null) {
            reservationRepository.deleteByBookingId(booking.getId());
            ticketRepository.deleteByBookingId(booking.getId());
            reservationRepository.flush();
            ticketRepository.flush();
            booking.updateDraft(showtime, subtotal, expiresAt);
        }

        booking = bookingRepository.saveAndFlush(booking);

        List<Seat> orderedSeats = request.seatIds().stream()
            .map(id -> seats.stream()
                .filter(seat -> id.equals(seat.getId()))
                .findFirst()
                .orElseThrow())
            .toList();

        List<BookingTicket> tickets = new ArrayList<>();
        for (TicketType type : ticketTypes) {
            tickets.add(new BookingTicket(booking, type, priceFor(showtime, type)));
        }
        tickets = ticketRepository.saveAllAndFlush(tickets);

        List<SeatReservation> reservations = new ArrayList<>();
        for (int index = 0; index < orderedSeats.size(); index++) {
            reservations.add(new SeatReservation(
                booking,
                showtime,
                orderedSeats.get(index),
                tickets.get(index),
                expiresAt
            ));
        }

        try {
            reservationRepository.saveAll(reservations);
            reservationRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "One or more selected seats were just reserved by another user.",
                exception
            );
        }

        return BookingResponse.from(booking, request.seatIds());
    }

    @Transactional
    public BookingResponse checkout(
        String sessionToken,
        User user,
        String contactEmail
    ) {
        Booking booking = bookingRepository.findBySessionToken(sessionToken)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No active booking was found for this session."
            ));

        if (booking.getStatus() != BookingStatus.DRAFT
            && booking.getStatus() != BookingStatus.PAYMENT_PENDING) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This booking cannot proceed to payment."
            );
        }

        List<SeatReservation> reservations =
            reservationRepository
                .findByBooking_IdOrderBySeat_RowLabelAscSeat_SeatNumberAsc(
                    booking.getId()
                );

        if (reservations.isEmpty()
            || (booking.getExpiresAt() != null
                && booking.getExpiresAt().isBefore(LocalDateTime.now()))) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The seat reservation has expired. Select seats again."
            );
        }

        booking.proceedToPayment(user, contactEmail.trim());
        bookingRepository.save(booking);

        List<Long> seatIds = reservations.stream()
            .map(reservation -> reservation.getSeat().getId())
            .toList();
        return BookingResponse.from(booking, seatIds);
    }

    @Transactional
    public BookingResponse confirm(
        String sessionToken,
        User user,
        ConfirmBookingRequest request
    ) {
        Booking booking = bookingRepository.findBySessionToken(sessionToken)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No booking was found for this session."
            ));
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return responseWithSeats(booking);
        }
        if (booking.getStatus() != BookingStatus.PAYMENT_PENDING
            || booking.getUser() == null
            || !booking.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This order is not ready for payment."
            );
        }
        List<SeatReservation> reservations = reservations(booking);
        if (reservations.isEmpty()
            || (booking.getExpiresAt() != null
                && booking.getExpiresAt().isBefore(LocalDateTime.now()))) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The seat reservation has expired. Select seats again."
            );
        }

        BigDecimal tax = booking.getSubtotal()
            .multiply(TAX_RATE)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = booking.getSubtotal().add(tax);
        CheckoutPaymentCardService.CheckoutCard checkoutCard =
            checkoutCardService.resolve(user, request);
        try {
            if (!paymentGateway.authorize(
                checkoutCard.number(),
                checkoutCard.expirationMonth(),
                checkoutCard.expirationYear(),
                checkoutCard.securityCode(),
                total
            ).approved()) {
                throw new PaymentRejectedException("Payment was declined.");
            }
        } catch (PaymentRejectedException exception) {
            throw new ResponseStatusException(
                HttpStatus.PAYMENT_REQUIRED,
                exception.getMessage(),
                exception
            );
        }

        String digits = checkoutCard.number().replaceAll("\\D", "");
        booking.confirm(
            tax,
            total,
            ConfirmationNumberGenerator.getInstance().next(),
            digits.substring(digits.length() - 4)
        );
        bookingRepository.saveAndFlush(booking);
        OrderHistoryResponse order = toOrderHistory(booking, reservations);
        emailService.sendOrderConfirmation(booking.getContactEmail(), order);
        return BookingResponse.from(
            booking,
            reservations.stream().map(item -> item.getSeat().getId()).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<OrderHistoryResponse> history(User user) {
        return bookingRepository
            .findAllByUser_IdAndStatusOrderByConfirmedAtDesc(
                user.getId(),
                BookingStatus.CONFIRMED
            )
            .stream()
            .map(booking -> toOrderHistory(booking, reservations(booking)))
            .toList();
    }

    private BookingResponse responseWithSeats(Booking booking) {
        List<Long> seats = reservations(booking).stream()
            .map(item -> item.getSeat().getId())
            .toList();
        return BookingResponse.from(booking, seats);
    }

    private List<SeatReservation> reservations(Booking booking) {
        return reservationRepository
            .findByBooking_IdOrderBySeat_RowLabelAscSeat_SeatNumberAsc(
                booking.getId()
            );
    }

    private OrderHistoryResponse toOrderHistory(
        Booking booking,
        List<SeatReservation> reservations
    ) {
        List<BookingTicket> tickets =
            ticketRepository.findByBooking_Id(booking.getId());
        return new OrderHistoryResponse(
            booking.getId(),
            booking.getConfirmationNumber(),
            booking.getShowtime().getMovie().getTitle(),
            booking.getShowtime().getStartsAt(),
            booking.getShowtime().getShowroom().getName(),
            reservations.stream().map(item -> item.getSeat().getLabel()).toList(),
            countTickets(tickets, TicketType.ADULT),
            countTickets(tickets, TicketType.CHILD),
            countTickets(tickets, TicketType.SENIOR),
            booking.getSubtotal(),
            booking.getTaxAmount(),
            booking.getTotalAmount(),
            booking.getCardLastFour(),
            booking.getConfirmedAt()
        );
    }

    private long countTickets(List<BookingTicket> tickets, TicketType type) {
        return tickets.stream()
            .filter(ticket -> ticket.getTicketType() == type)
            .count();
    }

    private List<TicketType> ticketTypes(ReserveSeatsRequest request) {
        List<TicketType> types = new ArrayList<>();
        addTypes(types, TicketType.ADULT, request.adultTickets());
        addTypes(types, TicketType.CHILD, request.childTickets());
        addTypes(types, TicketType.SENIOR, request.seniorTickets());
        return types;
    }

    private void addTypes(List<TicketType> types, TicketType type, int count) {
        for (int index = 0; index < count; index++) {
            types.add(type);
        }
    }

    private BigDecimal calculateSubtotal(
        Showtime showtime,
        ReserveSeatsRequest request
    ) {
        return showtime.getAdultPrice()
            .multiply(BigDecimal.valueOf(request.adultTickets()))
            .add(showtime.getChildPrice()
                .multiply(BigDecimal.valueOf(request.childTickets())))
            .add(showtime.getSeniorPrice()
                .multiply(BigDecimal.valueOf(request.seniorTickets())));
    }

    private BigDecimal priceFor(Showtime showtime, TicketType type) {
        return switch (type) {
            case ADULT -> showtime.getAdultPrice();
            case CHILD -> showtime.getChildPrice();
            case SENIOR -> showtime.getSeniorPrice();
        };
    }
}
