package com.kiran.flightbooking.controller;

import com.kiran.flightbooking.dto.response.FlightResponse;
import com.kiran.flightbooking.dto.response.PageResponse;
import com.kiran.flightbooking.enums.FlightStatus;
import com.kiran.flightbooking.security.CustomUserDetailsService;
import com.kiran.flightbooking.security.JwtTokenProvider;
import com.kiran.flightbooking.service.FlightService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for isolated controller endpoint test
@DisplayName("FlightController MockMvc Tests")
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService flightService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("GET /api/v1/flights returns paginated flights")
    void testSearchFlightsReturnsOk() throws Exception {
        FlightResponse flight = new FlightResponse();
        flight.setId(1L);
        flight.setFlightCode("PN101");
        flight.setAirline("Kiran Air");
        flight.setSource("PUNE");
        flight.setDestination("DELHI");
        flight.setTotalSeats(20);
        flight.setAvailableSeats(20);
        flight.setBaseFare(new BigDecimal("4000.00"));
        flight.setStatus(FlightStatus.SCHEDULED);
        flight.setDepartureTime(OffsetDateTime.now().plusDays(1));
        flight.setArrivalTime(OffsetDateTime.now().plusDays(1).plusHours(2));

        PageResponse<FlightResponse> pageResponse = PageResponse.from(
                new PageImpl<>(List.of(flight), PageRequest.of(0, 10), 1),
                List.of(flight)
        );

        when(flightService.searchFlights(any(), any(), any(), any(), any(), any(), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/flights")
                        .param("source", "PUNE")
                        .param("destination", "DELHI")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].flightCode").value("PN101"))
                .andExpect(jsonPath("$.content[0].airline").value("Kiran Air"))
                .andExpect(jsonPath("$.content[0].source").value("PUNE"))
                .andExpect(jsonPath("$.content[0].destination").value("DELHI"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/flights/{id} returns single flight details")
    void testGetFlightByIdReturnsOk() throws Exception {
        FlightResponse flight = new FlightResponse();
        flight.setId(1L);
        flight.setFlightCode("PN101");
        flight.setAirline("Kiran Air");
        flight.setSource("PUNE");
        flight.setDestination("DELHI");
        flight.setTotalSeats(20);
        flight.setAvailableSeats(20);
        flight.setBaseFare(new BigDecimal("4000.00"));
        flight.setStatus(FlightStatus.SCHEDULED);

        when(flightService.getFlightById(1L)).thenReturn(flight);

        mockMvc.perform(get("/api/v1/flights/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.flightCode").value("PN101"))
                .andExpect(jsonPath("$.baseFare").value(4000.00));
    }
}
