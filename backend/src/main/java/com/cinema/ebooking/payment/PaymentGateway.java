package com.cinema.ebooking.payment;

import java.math.BigDecimal;

public interface PaymentGateway {
    PaymentResult authorize(
        String cardNumber,
        int expirationMonth,
        int expirationYear,
        String securityCode,
        BigDecimal amount
    );
}
