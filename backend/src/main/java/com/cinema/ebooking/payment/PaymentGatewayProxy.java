package com.cinema.ebooking.payment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Proxy: validates and sanitizes access before delegating to the real gateway.
 */
@Component
public class PaymentGatewayProxy implements PaymentGateway {
    private final PaymentGateway target = new SimulatedPaymentGateway();

    @Override
    public PaymentResult authorize(
        String cardNumber,
        int expirationMonth,
        int expirationYear,
        String securityCode,
        BigDecimal amount
    ) {
        String digits = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
        if (!isLuhnValid(digits)) {
            throw new PaymentRejectedException("The card number is invalid.");
        }
        if (YearMonth.of(expirationYear, expirationMonth)
            .isBefore(YearMonth.now())) {
            throw new PaymentRejectedException("The payment card has expired.");
        }
        if (securityCode == null || !securityCode.matches("\\d{3,4}")) {
            throw new PaymentRejectedException("The security code is invalid.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new PaymentRejectedException("The order total is invalid.");
        }
        return target.authorize(
            digits,
            expirationMonth,
            expirationYear,
            securityCode,
            amount
        );
    }

    private boolean isLuhnValid(String value) {
        if (!value.matches("\\d{13,19}")) return false;
        int sum = 0;
        boolean doubleDigit = false;
        for (int index = value.length() - 1; index >= 0; index--) {
            int digit = value.charAt(index) - '0';
            if (doubleDigit && (digit *= 2) > 9) digit -= 9;
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
