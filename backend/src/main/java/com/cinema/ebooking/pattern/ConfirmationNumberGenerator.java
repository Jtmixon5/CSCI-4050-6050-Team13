package com.cinema.ebooking.pattern;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classic thread-safe Singleton used to generate human-readable order numbers.
 */
public final class ConfirmationNumberGenerator {
    private static final DateTimeFormatter TIME =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final SecureRandom random = new SecureRandom();

    private ConfirmationNumberGenerator() {
    }

    private static class Holder {
        private static final ConfirmationNumberGenerator INSTANCE =
            new ConfirmationNumberGenerator();
    }

    public static ConfirmationNumberGenerator getInstance() {
        return Holder.INSTANCE;
    }

    public synchronized String next() {
        return "CIN-" + LocalDateTime.now().format(TIME)
            + "-" + String.format("%06d", random.nextInt(1_000_000));
    }
}
