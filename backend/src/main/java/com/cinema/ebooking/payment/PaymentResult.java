package com.cinema.ebooking.payment;

public record PaymentResult(boolean approved, String transactionId) {
}
