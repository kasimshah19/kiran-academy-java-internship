package com.kiran.flightbooking.service;

import com.kiran.flightbooking.dto.response.FareBreakdownResponse;
import com.kiran.flightbooking.enums.PassengerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FareCalculationService Unit Tests")
class FareCalculationServiceTest {

    private FareCalculationService fareCalculationService;

    @BeforeEach
    void setUp() {
        fareCalculationService = new FareCalculationService(
                new BigDecimal("0.05"), // 5% tax
                new BigDecimal("50.00"), // 50.00 service fee
                new BigDecimal("100.00") // 100.00 cancellation fee
        );
    }

    @Test
    @DisplayName("Regular passenger receives 0% discount and pays base fare, 5% tax and 50.00 fee")
    void testRegularPassengerFareCalculation() {
        // Flight PN101 base fare = 4000.00, 2 tickets
        BigDecimal baseFare = new BigDecimal("4000.00");
        int ticketCount = 2;

        FareBreakdownResponse breakdown = fareCalculationService.calculateFare(
                baseFare, ticketCount, PassengerType.REGULAR, null
        );

        // Subtotal = 4000 * 2 = 8000.00
        assertEquals(new BigDecimal("8000.00"), breakdown.getSubtotal());
        // Passenger discount = 0.00
        assertEquals(new BigDecimal("0.00"), breakdown.getPassengerDiscount());
        assertEquals(new BigDecimal("0.00"), breakdown.getTotalDiscount());
        // Tax = 5% of 8000.00 = 400.00
        assertEquals(new BigDecimal("400.00"), breakdown.getTaxAmount());
        // Service fee = 50.00
        assertEquals(new BigDecimal("50.00"), breakdown.getServiceFee());
        // Total = 8000.00 + 400.00 + 50.00 = 8450.00
        assertEquals(new BigDecimal("8450.00"), breakdown.getTotalAmount());
    }

    @Test
    @DisplayName("Student passenger receives 10% discount on subtotal")
    void testStudentPassengerFareCalculation() {
        // Flight MB202 base fare = 3500.00, 1 ticket
        BigDecimal baseFare = new BigDecimal("3500.00");
        int ticketCount = 1;

        FareBreakdownResponse breakdown = fareCalculationService.calculateFare(
                baseFare, ticketCount, PassengerType.STUDENT, null
        );

        // Subtotal = 3500.00
        assertEquals(new BigDecimal("3500.00"), breakdown.getSubtotal());
        // 10% discount = 350.00
        assertEquals(new BigDecimal("350.00"), breakdown.getPassengerDiscount());
        assertEquals(new BigDecimal("350.00"), breakdown.getTotalDiscount());
        // Taxable base = 3500 - 350 = 3150.00
        // Tax = 5% of 3150.00 = 157.50
        assertEquals(new BigDecimal("157.50"), breakdown.getTaxAmount());
        // Service fee = 50.00
        assertEquals(new BigDecimal("50.00"), breakdown.getServiceFee());
        // Total = 3150.00 + 157.50 + 50.00 = 3357.50
        assertEquals(new BigDecimal("3357.50"), breakdown.getTotalAmount());
    }

    @Test
    @DisplayName("Promo code FLYPROMO500 deducts flat 500.00 from discounted subtotal")
    void testPromoCodeDiscount() {
        BigDecimal baseFare = new BigDecimal("3000.00");
        int ticketCount = 1;

        FareBreakdownResponse breakdown = fareCalculationService.calculateFare(
                baseFare, ticketCount, PassengerType.REGULAR, "FLYPROMO500"
        );

        // Subtotal = 3000.00
        assertEquals(new BigDecimal("3000.00"), breakdown.getSubtotal());
        assertEquals(new BigDecimal("0.00"), breakdown.getPassengerDiscount());
        assertEquals(new BigDecimal("500.00"), breakdown.getPromoDiscount());
        assertEquals(new BigDecimal("500.00"), breakdown.getTotalDiscount());
        // Taxable base = 3000 - 500 = 2500.00
        // Tax = 5% of 2500.00 = 125.00
        assertEquals(new BigDecimal("125.00"), breakdown.getTaxAmount());
        // Service fee = 50.00
        assertEquals(new BigDecimal("50.00"), breakdown.getServiceFee());
        // Total = 2500.00 + 125.00 + 50.00 = 2675.00
        assertEquals(new BigDecimal("2675.00"), breakdown.getTotalAmount());
    }

    @Test
    @DisplayName("Invalid or negative ticket count throws IllegalArgumentException")
    void testInvalidTicketCountThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                fareCalculationService.calculateFare(new BigDecimal("1000.00"), 0, PassengerType.REGULAR, null)
        );
        assertThrows(IllegalArgumentException.class, () ->
                fareCalculationService.calculateFare(new BigDecimal("1000.00"), -2, PassengerType.REGULAR, null)
        );
    }

    @Test
    @DisplayName("Negative base fare throws IllegalArgumentException")
    void testNegativeBaseFareThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                fareCalculationService.calculateFare(new BigDecimal("-100.00"), 1, PassengerType.REGULAR, null)
        );
    }

    @Test
    @DisplayName("Cancellation refund deducts cancellation fee and does not return negative")
    void testCancellationRefundCalculation() {
        BigDecimal totalPaid = new BigDecimal("3357.50");
        BigDecimal refund = fareCalculationService.calculateRefundAmount(totalPaid);
        // 3357.50 - 100.00 = 3257.50
        assertEquals(new BigDecimal("3257.50"), refund);

        // When total paid is less than cancellation fee
        BigDecimal smallPaid = new BigDecimal("80.00");
        BigDecimal smallRefund = fareCalculationService.calculateRefundAmount(smallPaid);
        assertEquals(new BigDecimal("0.00"), smallRefund);
    }
}
