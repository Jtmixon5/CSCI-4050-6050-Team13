package com.cinema.ebooking.service;

import com.cinema.ebooking.dto.ConfirmBookingRequest;
import com.cinema.ebooking.entity.PaymentCard;
import com.cinema.ebooking.entity.User;
import com.cinema.ebooking.repository.PaymentCardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Resolves either a user-owned saved card or a one-time checkout card.
 */
@Service
public class CheckoutPaymentCardService {
    private final PaymentCardRepository cardRepository;
    private final PaymentInformationEncryptionService encryptionService;

    public CheckoutPaymentCardService(
        PaymentCardRepository cardRepository,
        @Value("${PAYMENT_ENCRYPTION_KEY:change-this-local-development-key}")
        String paymentEncryptionKey
    ) {
        this.cardRepository = cardRepository;
        this.encryptionService =
            new PaymentInformationEncryptionService(paymentEncryptionKey);
    }

    public CheckoutCard resolve(User user, ConfirmBookingRequest request) {
        if (request.securityCode() == null
            || !request.securityCode().matches("\\d{3,4}")) {
            throw badRequest("Security code must contain 3 or 4 digits.");
        }
        if (request.savedCardId() != null) {
            PaymentCard saved = cardRepository
                .findByIdAndUserId(request.savedCardId(), user.getId())
                .orElseThrow(() -> badRequest(
                    "The selected saved payment card was not found."
                ));
            return new CheckoutCard(
                encryptionService.decrypt(saved.getEncryptedCardNumber()),
                Integer.parseInt(encryptionService.decrypt(
                    saved.getEncryptedExpirationMonth()
                )),
                Integer.parseInt(encryptionService.decrypt(
                    saved.getEncryptedExpirationYear()
                )),
                request.securityCode()
            );
        }
        if (request.cardNumber() == null
            || request.expirationMonth() == null
            || request.expirationYear() == null) {
            throw badRequest("Select a saved card or enter a payment card.");
        }
        return new CheckoutCard(
            request.cardNumber(),
            request.expirationMonth(),
            request.expirationYear(),
            request.securityCode()
        );
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public record CheckoutCard(
        String number,
        int expirationMonth,
        int expirationYear,
        String securityCode
    ) {
    }
}
