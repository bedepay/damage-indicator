package ru.dimas.damageindicator.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DamageMath {

    private DamageMath() {
    }

    public static double remainingHealth(double currentHealth, double damage) {
        return Math.max(0.0, currentHealth - Math.max(0.0, damage));
    }

    public static float healthProgress(double health, double maxHealth) {
        if (!Double.isFinite(health) || !Double.isFinite(maxHealth) || maxHealth <= 0.0) {
            return 0.0f;
        }
        return (float) Math.clamp(health / maxHealth, 0.0, 1.0);
    }

    public static String format(double value, int decimalPlaces) {
        return BigDecimal.valueOf(value)
                .setScale(decimalPlaces, RoundingMode.HALF_UP)
                .toPlainString();
    }
}
