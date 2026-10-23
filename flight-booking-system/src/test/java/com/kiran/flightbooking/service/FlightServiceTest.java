package com.kiran.flightbooking.service;

import com.kiran.flightbooking.dto.request.FlightCreateRequest;
import com.kiran.flightbooking.dto.response.FlightResponse;
import com.kiran.flightbooking.entity.Flight;
import com.kiran.flightbooking.enums.FlightStatus;
import com.kiran.flightbooking.exception.DuplicateResourceException;
import com.kiran.flightbooking.exception.InvalidBookingStateException;
import com.kiran.flightbooking.mapper.FlightMapper;
import com.kiran.flightbooking.repository.FlightRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FlightService Unit Tests")
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Spy
    private FlightMapper flightMapper;

    @InjectMocks
    private FlightService flightService;

    private FlightCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new FlightCreateRequest();
        createRequest.setFlightCode("KA555");
        createRequest.setAirline("Kiran Air");
        createRequest.setSource("PUNE");
        createRequest.setDestination("GOA");
        createRequest.setDepartureTime(OffsetDateTime.now().plusDays(5));
        createRequest.setArrivalTime(OffsetDateTime.now().plusDays(5).plusHours(1));
        createRequest.setTotalSeats(50);
        createRequest.setBaseFare(new BigDecimal("2500.00"));
    }

    @Test
    @DisplayName("Admin creates flight successfully")
    void testCreateFlightSuccess() {
        when(flightRepository.existsByFlightCode("KA555")).thenReturn(false);
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> {
            Flight f = invocation.getArgument(0);
            f.setId(50L);
            return f;
        });

        FlightResponse response = flightService.createFlight(createRequest);

        assertNotNull(response);
        assertEquals("KA555", response.getFlightCode());
        assertEquals("PUNE", response.getSource());
        assertEquals("GOA", response.getDestination());
        assertEquals(50, response.getAvailableSeats());
        assertEquals(FlightStatus.SCHEDULED, response.getStatus());
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    @Test
    @DisplayName("Create flight fails if flight code already exists")
    void testDuplicateFlightCodeThrowsException() {
        when(flightRepository.existsByFlightCode("KA555")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                flightService.createFlight(createRequest)
        );

        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    @DisplayName("Create flight fails if source and destination are identical")
    void testIdenticalSourceDestinationThrowsException() {
        createRequest.setDestination("PUNE"); // same as source
        when(flightRepository.existsByFlightCode("KA555")).thenReturn(false);

        assertThrows(InvalidBookingStateException.class, () ->
                flightService.createFlight(createRequest)
        );

        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    @DisplayName("Create flight fails if arrival time is before departure time")
    void testArrivalBeforeDepartureThrowsException() {
        createRequest.setArrivalTime(createRequest.getDepartureTime().minusHours(2));
        when(flightRepository.existsByFlightCode("KA555")).thenReturn(false);

        assertThrows(InvalidBookingStateException.class, () ->
                flightService.createFlight(createRequest)
        );

        verify(flightRepository, never()).save(any(Flight.class));
    }
}
