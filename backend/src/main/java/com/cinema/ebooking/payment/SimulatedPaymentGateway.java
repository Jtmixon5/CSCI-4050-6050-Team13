package com.cinema.ebooking.payment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Local payment processor for the course project. No raw card data is stored.
 */
final class SimulatedPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult authorize(
        String cardNumber,
        int expirationMonth,
        int expirationYear,
        String securityCode,
        BigDecimal amount
    ) {
        return new PaymentResult(
            amount.signum() > 0,
            "SIM-" + UUID.randomUUID()
        );
    }
}
