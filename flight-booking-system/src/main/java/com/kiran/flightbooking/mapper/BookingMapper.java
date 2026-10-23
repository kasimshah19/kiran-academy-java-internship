package com.kiran.flightbooking.mapper;

import com.kiran.flightbooking.dto.response.BookingResponse;
import com.kiran.flightbooking.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingReference(booking.getBookingReference());

        if (booking.getUser() != null) {
            response.setUserId(booking.getUser().getId());
            response.setUserEmail(booking.getUser().getEmail());
            response.setPassengerFullName(booking.getUser().getFullName());
        }

        if (booking.getFlight() != null) {
            response.setFlightId(booking.getFlight().getId());
            response.setFlightCode(booking.getFlight().getFlightCode());
            response.setAirline(booking.getFlight().getAirline());
            response.setSource(booking.getFlight().getSource());
            response.setDestination(booking.getFlight().getDestination());
            response.setDepartureTime(booking.getFlight().getDepartureTime());
            response.setArrivalTime(booking.getFlight().getArrivalTime());
            response.setBaseFare(booking.getFlight().getBaseFare());
        }

        response.setTicketCount(booking.getTicketCount());
        response.setPassengerType(booking.getPassengerType());
        response.setSubtotal(booking.getSubtotal());
        response.setDiscountAmount(booking.getDiscountAmount());
        response.setTaxAmount(booking.getTaxAmount());
        response.setServiceFee(booking.getServiceFee());
        response.setTotalAmount(booking.getTotalAmount());
        response.setStatus(booking.getStatus());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        response.setCancelledAt(booking.getCancelledAt());

        return response;
    }
}
