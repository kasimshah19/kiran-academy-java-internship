package com.kiran.flightbooking.dto.request;

import com.kiran.flightbooking.enums.PassengerType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BookingCreateRequest {

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotNull(message = "Ticket count is required")
    @Min(value = 1, message = "Ticket count must be at least 1")
    private Integer ticketCount;

    @NotNull(message = "Passenger type is required (REGULAR or STUDENT)")
    private PassengerType passengerType;

    private String couponCode;

    public BookingCreateRequest() {
    }

    public BookingCreateRequest(Long flightId, Integer ticketCount, PassengerType passengerType) {
        this.flightId = flightId;
        this.ticketCount = ticketCount;
        this.passengerType = passengerType;
    }

    public BookingCreateRequest(Long flightId, Integer ticketCount, PassengerType passengerType, String couponCode) {
        this.flightId = flightId;
        this.ticketCount = ticketCount;
        this.passengerType = passengerType;
        this.couponCode = couponCode;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public Integer getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(Integer ticketCount) {
        this.ticketCount = ticketCount;
    }

    public PassengerType getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(PassengerType passengerType) {
        this.passengerType = passengerType;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
