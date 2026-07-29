package com.cinema.ebooking.payment;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentGatewayProxyTest {
    private final PaymentGateway gateway = new PaymentGatewayProxy();

    @Test
    void delegatesValidPaymentToGateway() {
        YearMonth future = YearMonth.now().plusYears(1);
        PaymentResult result = gateway.authorize(
            "4242424242424242",
            future.getMonthValue(),
            future.getYear(),
            "123",
            new BigDecimal("25.00")
        );

        assertTrue(result.approved());
        assertTrue(result.transactionId().startsWith("SIM-"));
    }

    @Test
    void rejectsInvalidCardBeforeItReachesGateway() {
        YearMonth future = YearMonth.now().plusYears(1);
        assertThrows(
            PaymentRejectedException.class,
            () -> gateway.authorize(
                "4242424242424241",
                future.getMonthValue(),
                future.getYear(),
                "123",
                new BigDecimal("25.00")
            )
        );
    }
}
