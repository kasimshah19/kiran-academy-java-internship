package com.kiran.flightbooking.controller;

import com.kiran.flightbooking.dto.response.FlightResponse;
import com.kiran.flightbooking.dto.response.PageResponse;
import com.kiran.flightbooking.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/flights")
@Tag(name = "Flights", description = "Public endpoints for searching and viewing flight schedules and availability")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    @Operation(summary = "Search flights", description = "Search and filter scheduled flights with pagination and sorting")
    public ResponseEntity<PageResponse<FlightResponse>> searchFlights(
            @Parameter(description = "Source city/airport (e.g. Pune, Mumbai)")
            @RequestParam(required = false) String source,
            @Parameter(description = "Destination city/airport (e.g. Delhi, Bengaluru)")
            @RequestParam(required = false) String destination,
            @Parameter(description = "Departure date in ISO format (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate,
            @Parameter(description = "Minimum base fare filter")
            @RequestParam(required = false) BigDecimal minFare,
            @Parameter(description = "Maximum base fare filter")
            @RequestParam(required = false) BigDecimal maxFare,
            @Parameter(description = "Minimum available seats")
            @RequestParam(required = false) Integer minSeats,
            @PageableDefault(page = 0, size = 10, sort = "departureTime", direction = Sort.Direction.ASC) Pageable pageable) {

        PageResponse<FlightResponse> response = flightService.searchFlights(
                source, destination, departureDate, minFare, maxFare, minSeats, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get flight by ID", description = "Retrieves full flight details and current seat availability")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long id) {
        FlightResponse response = flightService.getFlightById(id);
        return ResponseEntity.ok(response);
    }
}
