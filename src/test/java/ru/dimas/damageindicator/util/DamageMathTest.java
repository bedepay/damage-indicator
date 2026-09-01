package ru.dimas.damageindicator.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DamageMathTest {

    @Test
    void remainingHealthNeverDropsBelowZero() {
        assertEquals(0.0, DamageMath.remainingHealth(4.0, 7.5));
    }

    @Test
    void healthProgressIsClamped() {
        assertEquals(1.0f, DamageMath.healthProgress(30.0, 20.0));
        assertEquals(0.0f, DamageMath.healthProgress(-3.0, 20.0));
        assertEquals(0.0f, DamageMath.healthProgress(10.0, 0.0));
    }

    @Test
    void formatUsesRequestedPrecisionAndHalfUpRounding() {
        assertEquals("12.35", DamageMath.format(12.345, 2));
        assertEquals("8", DamageMath.format(8.0, 0));
    }
}
