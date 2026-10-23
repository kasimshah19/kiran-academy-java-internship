package com.kiran.flightbooking.controller;

import com.kiran.flightbooking.dto.request.BookingCreateRequest;
import com.kiran.flightbooking.dto.response.BookingResponse;
import com.kiran.flightbooking.dto.response.FareBreakdownResponse;
import com.kiran.flightbooking.dto.response.PageResponse;
import com.kiran.flightbooking.enums.PassengerType;
import com.kiran.flightbooking.security.SecurityUtils;
import com.kiran.flightbooking.service.BookingService;
import com.kiran.flightbooking.service.FareCalculationService;
import com.kiran.flightbooking.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Endpoints for creating, viewing, and cancelling flight bookings")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;
    private final FlightService flightService;
    private final FareCalculationService fareCalculationService;

    public BookingController(BookingService bookingService,
                             FlightService flightService,
                             FareCalculationService fareCalculationService) {
        this.bookingService = bookingService;
        this.flightService = flightService;
        this.fareCalculationService = fareCalculationService;
    }

    @PostMapping
    @Operation(summary = "Create a flight booking", description = "Reserves seat inventory atomically, calculates fare with discounts and taxes, and creates a confirmed booking")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        Long authenticatedUserId = SecurityUtils.getCurrentUserId();
        BookingResponse response = bookingService.createBooking(authenticatedUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get booking history", description = "Retrieves paginated booking history for the authenticated passenger")
    public ResponseEntity<PageResponse<BookingResponse>> getMyBookings(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long authenticatedUserId = SecurityUtils.getCurrentUserId();
        PageResponse<BookingResponse> response = bookingService.getUserBookings(authenticatedUserId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bookingReference}")
    @Operation(summary = "Get booking by reference", description = "Retrieves booking details by reference code (owner or administrator only)")
    public ResponseEntity<BookingResponse> getBookingByReference(@PathVariable String bookingReference) {
        Long authenticatedUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();
        BookingResponse response = bookingService.getBookingByReference(bookingReference, authenticatedUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingReference}/cancel")
    @Operation(summary = "Cancel a booking", description = "Cancels an active booking, restores flight seat inventory, and prevents repeat cancellations")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable String bookingReference) {
        Long authenticatedUserId = SecurityUtils.getCurrentUserId();
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();
        BookingResponse response = bookingService.cancelBooking(bookingReference, authenticatedUserId, isAdmin);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/preview-fare")
    @Operation(summary = "Preview fare calculation", description = "Calculates itemized fare breakdown without making a booking")
    public ResponseEntity<FareBreakdownResponse> previewFare(
            @RequestParam BigDecimal baseFare,
            @RequestParam int ticketCount,
            @RequestParam(defaultValue = "REGULAR") PassengerType passengerType,
            @RequestParam(required = false) String promoCode) {
        FareBreakdownResponse breakdown = fareCalculationService.calculateFare(baseFare, ticketCount, passengerType, promoCode);
        return ResponseEntity.ok(breakdown);
    }
}
