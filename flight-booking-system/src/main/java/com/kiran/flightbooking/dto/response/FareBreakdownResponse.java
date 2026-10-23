package com.kiran.flightbooking.dto.response;

import java.math.BigDecimal;

public class FareBreakdownResponse {

    private BigDecimal baseFarePerTicket;
    private int ticketCount;
    private BigDecimal subtotal;
    private double discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal discountedSubtotal;
    private BigDecimal taxAmount;
    private BigDecimal serviceFee;
    private BigDecimal totalAmount;

    public FareBreakdownResponse() {
    }

    public FareBreakdownResponse(BigDecimal baseFarePerTicket, int ticketCount, BigDecimal subtotal,
                                 double discountPercentage, BigDecimal discountAmount, BigDecimal discountedSubtotal,
                                 BigDecimal taxAmount, BigDecimal serviceFee, BigDecimal totalAmount) {
        this.baseFarePerTicket = baseFarePerTicket;
        this.ticketCount = ticketCount;
        this.subtotal = subtotal;
        this.discountPercentage = discountPercentage;
        this.discountAmount = discountAmount;
        this.discountedSubtotal = discountedSubtotal;
        this.taxAmount = taxAmount;
        this.serviceFee = serviceFee;
        this.totalAmount = totalAmount;
    }

    public BigDecimal getBaseFarePerTicket() {
        return baseFarePerTicket;
    }

    public void setBaseFarePerTicket(BigDecimal baseFarePerTicket) {
        this.baseFarePerTicket = baseFarePerTicket;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountedSubtotal() {
        return discountedSubtotal;
    }

    public void setDiscountedSubtotal(BigDecimal discountedSubtotal) {
        this.discountedSubtotal = discountedSubtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public void setServiceFee(BigDecimal serviceFee) {
        this.serviceFee = serviceFee;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
