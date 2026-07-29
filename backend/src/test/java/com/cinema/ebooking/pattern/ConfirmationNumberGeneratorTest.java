package com.cinema.ebooking.pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConfirmationNumberGeneratorTest {
    @Test
    void exposesOneSharedInstanceAndGeneratesUniqueNumbers() {
        ConfirmationNumberGenerator first =
            ConfirmationNumberGenerator.getInstance();
        ConfirmationNumberGenerator second =
            ConfirmationNumberGenerator.getInstance();

        assertSame(first, second);
        assertNotEquals(first.next(), second.next());
    }
}
