package com.kiran.flightbooking.controller;

import com.kiran.flightbooking.dto.request.FlightCreateRequest;
import com.kiran.flightbooking.dto.request.FlightStatusUpdateRequest;
import com.kiran.flightbooking.dto.request.FlightUpdateRequest;
import com.kiran.flightbooking.dto.response.FlightResponse;
import com.kiran.flightbooking.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/flights")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Flight Administration", description = "Admin-only endpoints for managing flights and schedules")
@SecurityRequirement(name = "bearerAuth")
public class AdminFlightController {

    private final FlightService flightService;

    public AdminFlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping
    @Operation(summary = "Create a new flight", description = "Administrators can schedule a new flight")
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody FlightCreateRequest request) {
        FlightResponse response = flightService.createFlight(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update flight details", description = "Administrators can update route, times and base fare of a flight")
    public ResponseEntity<FlightResponse> updateFlight(@PathVariable Long id, @Valid @RequestBody FlightUpdateRequest request) {
        FlightResponse response = flightService.updateFlight(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update flight status", description = "Administrators can update status (e.g. SCHEDULED, DELAYED, CANCELLED, COMPLETED)")
    public ResponseEntity<FlightResponse> updateFlightStatus(@PathVariable Long id, @Valid @RequestBody FlightStatusUpdateRequest request) {
        FlightResponse response = flightService.updateFlightStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a flight schedule", description = "Administrators can cancel an existing flight schedule")
    public ResponseEntity<FlightResponse> cancelFlight(@PathVariable Long id) {
        FlightResponse response = flightService.cancelFlight(id);
        return ResponseEntity.ok(response);
    }
}
