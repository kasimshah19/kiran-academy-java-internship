package com.kiran.flightbooking.service;

import com.kiran.flightbooking.dto.request.BookingCreateRequest;
import com.kiran.flightbooking.dto.response.BookingResponse;
import com.kiran.flightbooking.dto.response.FareBreakdownResponse;
import com.kiran.flightbooking.entity.Booking;
import com.kiran.flightbooking.entity.Flight;
import com.kiran.flightbooking.entity.User;
import com.kiran.flightbooking.enums.BookingStatus;
import com.kiran.flightbooking.enums.FlightStatus;
import com.kiran.flightbooking.enums.PassengerType;
import com.kiran.flightbooking.enums.Role;
import com.kiran.flightbooking.exception.InsufficientSeatsException;
import com.kiran.flightbooking.exception.InvalidBookingStateException;
import com.kiran.flightbooking.exception.UnauthorizedAccessException;
import com.kiran.flightbooking.mapper.BookingMapper;
import com.kiran.flightbooking.repository.BookingRepository;
import com.kiran.flightbooking.repository.FlightRepository;
import com.kiran.flightbooking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FareCalculationService fareCalculationService;

    @Spy
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    private User sampleUser;
    private Flight sampleFlight;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setEmail("passenger@example.com");
        sampleUser.setFullName("John Doe");
        sampleUser.setRole(Role.PASSENGER);

        sampleFlight = new Flight();
        sampleFlight.setId(10L);
        sampleFlight.setFlightCode("PN101");
        sampleFlight.setAirline("Kiran Air");
        sampleFlight.setSource("PUNE");
        sampleFlight.setDestination("DELHI");
        sampleFlight.setDepartureTime(OffsetDateTime.now().plusDays(2));
        sampleFlight.setArrivalTime(OffsetDateTime.now().plusDays(2).plusHours(2));
        sampleFlight.setTotalSeats(20);
        sampleFlight.setAvailableSeats(20);
        sampleFlight.setBaseFare(new BigDecimal("4000.00"));
        sampleFlight.setStatus(FlightStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Successfully creates booking and reserves inventory atomically")
    void testCreateBookingSuccess() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setFlightId(10L);
        request.setTicketCount(2);
        request.setPassengerType(PassengerType.REGULAR);

        FareBreakdownResponse fareResponse = new FareBreakdownResponse(
                new BigDecimal("4000.00"), 2, PassengerType.REGULAR,
                new BigDecimal("8000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("0.05"), new BigDecimal("400.00"), new BigDecimal("50.00"),
                new BigDecimal("8450.00")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(flightRepository.findById(10L)).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.reserveSeats(10L, 2)).thenReturn(1);
        when(fareCalculationService.calculateFare(any(), eq(2), eq(PassengerType.REGULAR), any())).thenReturn(fareResponse);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(100L);
            return b;
        });

        BookingResponse response = bookingService.createBooking(1L, request);

        assertNotNull(response);
        assertEquals(2, response.getTicketCount());
        assertEquals(new BigDecimal("8450.00"), response.getTotalAmount());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertTrue(response.getBookingReference().startsWith("BK-"));

        verify(flightRepository, times(1)).reserveSeats(10L, 2);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Booking fails when insufficient seats are available")
    void testCreateBookingInsufficientSeats() {
        BookingCreateRequest request = new BookingCreateRequest();
        request.setFlightId(10L);
        request.setTicketCount(5);
        request.setPassengerType(PassengerType.REGULAR);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(flightRepository.findById(10L)).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.reserveSeats(10L, 5)).thenReturn(0); // 0 rows updated -> inventory check failed

        assertThrows(InsufficientSeatsException.class, () ->
                bookingService.createBooking(1L, request)
        );

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Booking fails when flight is cancelled")
    void testCreateBookingOnCancelledFlight() {
        sampleFlight.setStatus(FlightStatus.CANCELLED);

        BookingCreateRequest request = new BookingCreateRequest();
        request.setFlightId(10L);
        request.setTicketCount(1);
        request.setPassengerType(PassengerType.REGULAR);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(flightRepository.findById(10L)).thenReturn(Optional.of(sampleFlight));

        assertThrows(InvalidBookingStateException.class, () ->
                bookingService.createBooking(1L, request)
        );

        verify(flightRepository, never()).reserveSeats(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Successfully cancels booking and releases inventory")
    void testCancelBookingSuccess() {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBookingReference("BK-TEST-123");
        booking.setUser(sampleUser);
        booking.setFlight(sampleFlight);
        booking.setTicketCount(2);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByBookingReference("BK-TEST-123")).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse response = bookingService.cancelBooking("BK-TEST-123", 1L, false);

        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        assertNotNull(response.getCancelledAt());
        verify(flightRepository, times(1)).releaseSeats(10L, 2);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Repeat cancellation throws InvalidBookingStateException")
    void testRepeatCancellationThrowsException() {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBookingReference("BK-TEST-123");
        booking.setUser(sampleUser);
        booking.setFlight(sampleFlight);
        booking.setTicketCount(2);
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findByBookingReference("BK-TEST-123")).thenReturn(Optional.of(booking));

        assertThrows(InvalidBookingStateException.class, () ->
                bookingService.cancelBooking("BK-TEST-123", 1L, false)
        );

        verify(flightRepository, never()).releaseSeats(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Cancelling another user's booking throws UnauthorizedAccessException")
    void testUnauthorizedCancellationThrowsException() {
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBookingReference("BK-TEST-123");
        booking.setUser(sampleUser); // owned by user 1
        booking.setFlight(sampleFlight);
        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingRepository.findByBookingReference("BK-TEST-123")).thenReturn(Optional.of(booking));

        // User 99 tries to cancel without being admin
        assertThrows(UnauthorizedAccessException.class, () ->
                bookingService.cancelBooking("BK-TEST-123", 99L, false)
        );

        verify(flightRepository, never()).releaseSeats(anyLong(), anyInt());
    }
}
