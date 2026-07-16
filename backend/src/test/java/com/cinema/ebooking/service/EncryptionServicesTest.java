package com.cinema.ebooking.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class EncryptionServicesTest {

    @Test
    void passwordHashingShouldProduceSecureHashAndValidateMatches() {
        PasswordEncryptionService service = new PasswordEncryptionService(10);

        String rawPassword = "MySecurePassword123!";
        String hash = service.hash(rawPassword);

        assertNotNull(hash);
        assertNotEquals(rawPassword, hash);
        assertTrue(service.matches(rawPassword, hash));
        assertFalse(service.matches("WrongPassword", hash));
    }

    @Test
    void paymentEncryptionShouldRoundTripPlaintext() {
        PaymentInformationEncryptionService service = new PaymentInformationEncryptionService("test-secret-key-32-bytes-long");

        String plaintext = "4111-1111-1111-1111";
        String encrypted = service.encrypt(plaintext);

        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);
        assertTrue(service.decrypt(encrypted).contains("4111"));
        assertTrue(service.decrypt(encrypted).endsWith("1111"));
    }
}
