package com.kiran.flightbooking.mapper;

import com.kiran.flightbooking.dto.request.FlightCreateRequest;
import com.kiran.flightbooking.dto.request.FlightUpdateRequest;
import com.kiran.flightbooking.dto.response.FlightResponse;
import com.kiran.flightbooking.entity.Flight;
import com.kiran.flightbooking.enums.FlightStatus;
import org.springframework.stereotype.Component;

@Component
public class FlightMapper {

    public FlightResponse toResponse(Flight flight) {
        if (flight == null) {
            return null;
        }
        FlightResponse response = new FlightResponse();
        response.setId(flight.getId());
        response.setFlightCode(flight.getFlightCode());
        response.setAirline(flight.getAirline());
        response.setSource(flight.getSource());
        response.setDestination(flight.getDestination());
        response.setDepartureTime(flight.getDepartureTime());
        response.setArrivalTime(flight.getArrivalTime());
        response.setTotalSeats(flight.getTotalSeats());
        response.setAvailableSeats(flight.getAvailableSeats());
        response.setBaseFare(flight.getBaseFare());
        response.setStatus(flight.getStatus());
        response.setCreatedAt(flight.getCreatedAt());
        response.setUpdatedAt(flight.getUpdatedAt());
        return response;
    }

    public Flight toEntity(FlightCreateRequest request) {
        if (request == null) {
            return null;
        }
        Flight flight = new Flight();
        flight.setFlightCode(request.getFlightCode().trim().toUpperCase());
        flight.setAirline(request.getAirline().trim());
        flight.setSource(request.getSource().trim().toUpperCase());
        flight.setDestination(request.getDestination().trim().toUpperCase());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setTotalSeats(request.getTotalSeats());
        flight.setAvailableSeats(request.getTotalSeats());
        flight.setBaseFare(request.getBaseFare());
        flight.setStatus(FlightStatus.SCHEDULED);
        return flight;
    }

    public void updateEntityFromRequest(Flight flight, FlightUpdateRequest request) {
        if (flight == null || request == null) {
            return;
        }
        flight.setAirline(request.getAirline().trim());
        flight.setSource(request.getSource().trim().toUpperCase());
        flight.setDestination(request.getDestination().trim().toUpperCase());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setBaseFare(request.getBaseFare());
    }
}
