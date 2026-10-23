package com.kiran.flightbooking.service;

import com.kiran.flightbooking.dto.request.BookingCreateRequest;
import com.kiran.flightbooking.dto.response.BookingResponse;
import com.kiran.flightbooking.dto.response.FareBreakdownResponse;
import com.kiran.flightbooking.dto.response.PageResponse;
import com.kiran.flightbooking.entity.Booking;
import com.kiran.flightbooking.entity.Flight;
import com.kiran.flightbooking.entity.User;
import com.kiran.flightbooking.enums.BookingStatus;
import com.kiran.flightbooking.enums.FlightStatus;
import com.kiran.flightbooking.exception.InsufficientSeatsException;
import com.kiran.flightbooking.exception.InvalidBookingStateException;
import com.kiran.flightbooking.exception.ResourceNotFoundException;
import com.kiran.flightbooking.exception.UnauthorizedAccessException;
import com.kiran.flightbooking.mapper.BookingMapper;
import com.kiran.flightbooking.repository.BookingRepository;
import com.kiran.flightbooking.repository.FlightRepository;
import com.kiran.flightbooking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final FareCalculationService fareCalculationService;
    private final BookingMapper bookingMapper;

    public BookingService(BookingRepository bookingRepository,
                          FlightRepository flightRepository,
                          UserRepository userRepository,
                          FareCalculationService fareCalculationService,
                          BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.userRepository = userRepository;
        this.fareCalculationService = fareCalculationService;
        this.bookingMapper = bookingMapper;
    }

    @Transactional
    public BookingResponse createBooking(Long userId, BookingCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + request.getFlightId()));

        if (flight.getStatus() == FlightStatus.CANCELLED || flight.getStatus() == FlightStatus.COMPLETED) {
            throw new InvalidBookingStateException("Cannot book tickets for flight with status: " + flight.getStatus());
        }

        if (flight.getDepartureTime().isBefore(OffsetDateTime.now())) {
            throw new InvalidBookingStateException("Cannot book tickets for a flight that has already departed");
        }

        int requestedSeats = request.getTicketCount();
        if (requestedSeats <= 0) {
            throw new IllegalArgumentException("Ticket count must be greater than zero");
        }

        // Concurrency-safe atomic seat inventory reservation
        int updatedRows = flightRepository.reserveSeats(flight.getId(), requestedSeats);
        if (updatedRows == 0) {
            log.warn("Booking failed: Insufficient seats on flight {} for requested count {}", flight.getFlightCode(), requestedSeats);
            throw new InsufficientSeatsException("Flight " + flight.getFlightCode() + " does not have " + requestedSeats + " seats available");
        }

        // Fare calculation with BigDecimal
        FareBreakdownResponse fareBreakdown = fareCalculationService.calculateFare(
                flight.getBaseFare(),
                requestedSeats,
                request.getPassengerType(),
                request.getPromoCode()
        );

        String bookingReference = generateBookingReference();

        Booking booking = new Booking();
        booking.setBookingReference(bookingReference);
        booking.setUser(user);
        booking.setFlight(flight);
        booking.setTicketCount(requestedSeats);
        booking.setPassengerType(request.getPassengerType());
        booking.setSubtotal(fareBreakdown.getSubtotal());
        booking.setDiscountAmount(fareBreakdown.getTotalDiscount());
        booking.setTaxAmount(fareBreakdown.getTaxAmount());
        booking.setServiceFee(fareBreakdown.getServiceFee());
        booking.setTotalAmount(fareBreakdown.getTotalAmount());
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Successfully created booking ref={} user={} flight={} seats={} total={}",
                savedBooking.getBookingReference(), user.getEmail(), flight.getFlightCode(), requestedSeats, savedBooking.getTotalAmount());

        return bookingMapper.toResponse(savedBooking);
    }

    @Transactional
    public BookingResponse cancelBooking(String bookingReference, Long authenticatedUserId, boolean isAdmin) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingReference));

        if (!isAdmin && !booking.getUser().getId().equals(authenticatedUserId)) {
            log.warn("Security alert: User id={} attempted to cancel booking ref={} owned by user id={}",
                    authenticatedUserId, bookingReference, booking.getUser().getId());
            throw new UnauthorizedAccessException("You are not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException("Booking " + bookingReference + " is already cancelled");
        }

        if (booking.getFlight().getDepartureTime().isBefore(OffsetDateTime.now())) {
            throw new InvalidBookingStateException("Cannot cancel a booking for a flight that has already departed");
        }

        // Concurrency-safe atomic seat inventory release
        flightRepository.releaseSeats(booking.getFlight().getId(), booking.getTicketCount());

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(OffsetDateTime.now());

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Booking cancelled ref={} seatsRestored={}", bookingReference, booking.getTicketCount());

        return bookingMapper.toResponse(updatedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingReference, Long authenticatedUserId, boolean isAdmin) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingReference));

        if (!isAdmin && !booking.getUser().getId().equals(authenticatedUserId)) {
            throw new UnauthorizedAccessException("You are not authorized to view this booking");
        }

        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getUserBookings(Long userId, Pageable pageable) {
        Page<Booking> page = bookingRepository.findAllByUserId(userId, pageable);
        List<BookingResponse> content = page.getContent().stream()
                .map(bookingMapper::toResponse)
                .toList();
        return PageResponse.from(page, content);
    }

    private String generateBookingReference() {
        return "BK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
