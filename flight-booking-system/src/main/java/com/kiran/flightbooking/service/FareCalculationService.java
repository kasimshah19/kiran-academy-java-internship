package com.kiran.flightbooking.service;

import com.kiran.flightbooking.dto.response.FareBreakdownResponse;
import com.kiran.flightbooking.enums.PassengerType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FareCalculationService {

    public static final int CURRENCY_SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static final BigDecimal STUDENT_DISCOUNT_PERCENT = new BigDecimal("0.10"); // 10%
    public static final BigDecimal REGULAR_DISCOUNT_PERCENT = BigDecimal.ZERO; // 0%

    private final BigDecimal taxRate;
    private final BigDecimal serviceFee;
    private final BigDecimal cancellationFee;

    public FareCalculationService(
            @Value("${app.pricing.tax-rate:0.05}") BigDecimal taxRate,
            @Value("${app.pricing.service-fee:50.00}") BigDecimal serviceFee,
            @Value("${app.pricing.cancellation-fee:100.00}") BigDecimal cancellationFee) {
        this.taxRate = taxRate.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        this.serviceFee = serviceFee.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        this.cancellationFee = cancellationFee.setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }

    public FareBreakdownResponse calculateFare(BigDecimal baseFare, int ticketCount, PassengerType passengerType, String promoCode) {
        if (baseFare == null || baseFare.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base fare must be non-negative");
        }
        if (ticketCount <= 0) {
            throw new IllegalArgumentException("Ticket count must be positive");
        }
        if (passengerType == null) {
            passengerType = PassengerType.REGULAR;
        }

        BigDecimal scaledBaseFare = baseFare.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        BigDecimal count = BigDecimal.valueOf(ticketCount);

        // 1. Subtotal = base fare * ticket count
        BigDecimal subtotal = scaledBaseFare.multiply(count).setScale(CURRENCY_SCALE, ROUNDING_MODE);

        // 2. Passenger Discount: Student = 10%, Regular = 0%
        BigDecimal discountPercent = (passengerType == PassengerType.STUDENT)
                ? STUDENT_DISCOUNT_PERCENT
                : REGULAR_DISCOUNT_PERCENT;

        BigDecimal passengerDiscount = subtotal.multiply(discountPercent).setScale(CURRENCY_SCALE, ROUNDING_MODE);

        // 3. Discounted Subtotal
        BigDecimal discountedSubtotal = subtotal.subtract(passengerDiscount).setScale(CURRENCY_SCALE, ROUNDING_MODE);

        // 4. Promotional code discount (Optional)
        BigDecimal promoDiscount = BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        if (promoCode != null && !promoCode.trim().isEmpty()) {
            promoDiscount = evaluatePromoCode(promoCode.trim().toUpperCase(), discountedSubtotal);
        }

        BigDecimal totalDiscount = passengerDiscount.add(promoDiscount).setScale(CURRENCY_SCALE, ROUNDING_MODE);
        BigDecimal taxableBase = subtotal.subtract(totalDiscount).max(BigDecimal.ZERO).setScale(CURRENCY_SCALE, ROUNDING_MODE);

        // 5. Tax (e.g., 5% of taxable base)
        BigDecimal calculatedTax = taxableBase.multiply(taxRate).setScale(CURRENCY_SCALE, ROUNDING_MODE);

        // 6. Service fee (flat)
        BigDecimal calculatedServiceFee = this.serviceFee;

        // 7. Final total amount
        BigDecimal totalAmount = taxableBase.add(calculatedTax).add(calculatedServiceFee).setScale(CURRENCY_SCALE, ROUNDING_MODE);

        return new FareBreakdownResponse(
                scaledBaseFare,
                ticketCount,
                passengerType,
                subtotal,
                passengerDiscount,
                promoDiscount,
                totalDiscount,
                taxRate,
                calculatedTax,
                calculatedServiceFee,
                totalAmount
        );
    }

    private BigDecimal evaluatePromoCode(String promoCode, BigDecimal currentSubtotal) {
        if ("FLYPROMO500".equals(promoCode)) {
            BigDecimal flatDiscount = new BigDecimal("500.00").setScale(CURRENCY_SCALE, ROUNDING_MODE);
            return flatDiscount.min(currentSubtotal);
        } else if ("SAVE10".equals(promoCode)) {
            return currentSubtotal.multiply(new BigDecimal("0.10")).setScale(CURRENCY_SCALE, ROUNDING_MODE);
        }
        return BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }

    public BigDecimal calculateRefundAmount(BigDecimal originalTotalAmount) {
        if (originalTotalAmount == null || originalTotalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(CURRENCY_SCALE, ROUNDING_MODE);
        }
        BigDecimal refundable = originalTotalAmount.subtract(cancellationFee);
        return refundable.max(BigDecimal.ZERO).setScale(CURRENCY_SCALE, ROUNDING_MODE);
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public BigDecimal getServiceFee() {
        return serviceFee;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }
}
