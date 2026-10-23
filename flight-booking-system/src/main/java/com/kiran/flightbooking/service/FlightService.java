package com.kiran.flightbooking.service;

import com.kiran.flightbooking.dto.request.FlightCreateRequest;
import com.kiran.flightbooking.dto.request.FlightStatusUpdateRequest;
import com.kiran.flightbooking.dto.request.FlightUpdateRequest;
import com.kiran.flightbooking.dto.response.FlightResponse;
import com.kiran.flightbooking.dto.response.PageResponse;
import com.kiran.flightbooking.entity.Flight;
import com.kiran.flightbooking.enums.FlightStatus;
import com.kiran.flightbooking.exception.DuplicateResourceException;
import com.kiran.flightbooking.exception.InvalidBookingStateException;
import com.kiran.flightbooking.exception.ResourceNotFoundException;
import com.kiran.flightbooking.mapper.FlightMapper;
import com.kiran.flightbooking.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class FlightService {

    private static final Logger log = LoggerFactory.getLogger(FlightService.class);

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    public FlightService(FlightRepository flightRepository, FlightMapper flightMapper) {
        this.flightRepository = flightRepository;
        this.flightMapper = flightMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<FlightResponse> searchFlights(
            String source,
            String destination,
            LocalDate departureDate,
            BigDecimal minFare,
            BigDecimal maxFare,
            Integer minSeats,
            Pageable pageable) {

        Specification<Flight> spec = Specification.where(null);

        // Public searches only show bookable flights (SCHEDULED or DELAYED)
        spec = spec.and((root, query, cb) -> root.get("status").in(FlightStatus.SCHEDULED, FlightStatus.DELAYED));

        if (source != null && !source.trim().isEmpty()) {
            String s = source.trim().toUpperCase();
            spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("source")), s));
        }

        if (destination != null && !destination.trim().isEmpty()) {
            String d = destination.trim().toUpperCase();
            spec = spec.and((root, query, cb) -> cb.equal(cb.upper(root.get("destination")), d));
        }

        if (departureDate != null) {
            OffsetDateTime startOfDay = departureDate.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime endOfDay = departureDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);
            spec = spec.and((root, query, cb) -> cb.between(root.get("departureTime"), startOfDay, endOfDay));
        }

        if (minFare != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("baseFare"), minFare));
        }

        if (maxFare != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("baseFare"), maxFare));
        }

        if (minSeats != null && minSeats > 0) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("availableSeats"), minSeats));
        }

        Page<Flight> page = flightRepository.findAll(spec, pageable);
        List<FlightResponse> content = page.getContent().stream()
                .map(flightMapper::toResponse)
                .toList();

        return PageResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public FlightResponse getFlightById(Long id) {
        Flight flight = findFlightEntityById(id);
        return flightMapper.toResponse(flight);
    }

    @Transactional(readOnly = true)
    public Flight findFlightEntityById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
    }

    @Transactional
    public FlightResponse createFlight(FlightCreateRequest request) {
        String code = request.getFlightCode().trim().toUpperCase();
        if (flightRepository.existsByFlightCode(code)) {
            throw new DuplicateResourceException("Flight code " + code + " already exists");
        }

        if (request.getSource().trim().equalsIgnoreCase(request.getDestination().trim())) {
            throw new InvalidBookingStateException("Source and destination cannot be identical");
        }

        if (!request.getArrivalTime().isAfter(request.getDepartureTime())) {
            throw new InvalidBookingStateException("Arrival time must be after departure time");
        }

        Flight flight = flightMapper.toEntity(request);
        Flight saved = flightRepository.save(flight);
        log.info("Created new flight id={} code={}", saved.getId(), saved.getFlightCode());
        return flightMapper.toResponse(saved);
    }

    @Transactional
    public FlightResponse updateFlight(Long id, FlightUpdateRequest request) {
        Flight flight = findFlightEntityById(id);

        if (request.getSource().trim().equalsIgnoreCase(request.getDestination().trim())) {
            throw new InvalidBookingStateException("Source and destination cannot be identical");
        }

        if (!request.getArrivalTime().isAfter(request.getDepartureTime())) {
            throw new InvalidBookingStateException("Arrival time must be after departure time");
        }

        flightMapper.updateEntityFromRequest(flight, request);
        Flight updated = flightRepository.save(flight);
        log.info("Updated flight id={} code={}", updated.getId(), updated.getFlightCode());
        return flightMapper.toResponse(updated);
    }

    @Transactional
    public FlightResponse updateFlightStatus(Long id, FlightStatusUpdateRequest request) {
        Flight flight = findFlightEntityById(id);
        flight.setStatus(request.getStatus());
        Flight updated = flightRepository.save(flight);
        log.info("Updated status for flight id={} to {}", updated.getId(), updated.getStatus());
        return flightMapper.toResponse(updated);
    }

    @Transactional
    public FlightResponse cancelFlight(Long id) {
        Flight flight = findFlightEntityById(id);
        flight.setStatus(FlightStatus.CANCELLED);
        Flight updated = flightRepository.save(flight);
        log.info("Flight id={} code={} has been cancelled by administrator", updated.getId(), updated.getFlightCode());
        return flightMapper.toResponse(updated);
    }
}
