package ru.dimas.damageindicator.config;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import ru.dimas.damageindicator.util.DisplayText;

import java.util.logging.Logger;

public record CombatDisplayConfig(
        int decimalPlaces,
        BossBarSettings bossBar,
        IndicatorSettings indicators
) {

    private static final String LEGACY_BOSSBAR_TITLE = "<gold><target></gold> <dark_gray>•</dark_gray> <red>Здоровье:</red> <white><health>/<max_health> ❤</white> <dark_gray>•</dark_gray> <yellow>Урон: -<damage></yellow> <critical>";
    private static final String DEFAULT_BOSSBAR_TITLE = "<gold><target></gold> <dark_gray>•</dark_gray> <red><health>/<max_health> ❤</red> <dark_gray>•</dark_gray> <damage>";
    private static final String DEFAULT_BOSSBAR_DAMAGE_TEXT = "<bold><gradient:#ffd166:#ff6b35>-<amount></gradient></bold>";
    private static final String DEFAULT_BOSSBAR_CRITICAL_DAMAGE_TEXT = "<bold><gradient:#fff06a:#ff8a00:#ff1744>✦ -<amount> ✦</gradient></bold>";
    private static final String DEFAULT_DAMAGE_TEXT = "<bold><gradient:#ff6b6b:#ff0000>-<amount> ❤</gradient></bold>";
    private static final String DEFAULT_CRITICAL_TEXT = "<bold><gradient:#fff06a:#ff8a00:#ff1744>✦ КРИТ -<amount> ✦</gradient></bold>";
    private static final String DEFAULT_HEALING_TEXT = "<bold><gradient:#7dff8a:#20d65a>+<amount> ❤</gradient></bold>";

    public static CombatDisplayConfig load(FileConfiguration config, Logger logger) {
        int decimalPlaces = boundedInt(config, logger, "decimal-places", 1, 0, 3);

        String bossBarDamageText = validIndicatorTemplate(
                config.getString("bossbar.damage-text", DEFAULT_BOSSBAR_DAMAGE_TEXT),
                DEFAULT_BOSSBAR_DAMAGE_TEXT,
                "bossbar.damage-text",
                decimalPlaces,
                logger
        );
        String bossBarCriticalDamageText = validIndicatorTemplate(
                config.getString("bossbar.critical-damage-text", DEFAULT_BOSSBAR_CRITICAL_DAMAGE_TEXT),
                DEFAULT_BOSSBAR_CRITICAL_DAMAGE_TEXT,
                "bossbar.critical-damage-text",
                decimalPlaces,
                logger
        );
        String configuredBossBarTitle = config.getString("bossbar.title", DEFAULT_BOSSBAR_TITLE);
        if (LEGACY_BOSSBAR_TITLE.equals(configuredBossBarTitle)) {
            configuredBossBarTitle = DEFAULT_BOSSBAR_TITLE;
        }
        String bossBarTitle = validBossBarTemplate(
                configuredBossBarTitle,
                DEFAULT_BOSSBAR_TITLE,
                bossBarDamageText,
                decimalPlaces,
                logger
        );

        BossBarSettings bossBar = new BossBarSettings(
                config.getBoolean("bossbar.enabled", true),
                boundedInt(config, logger, "bossbar.duration-ticks", 60, 1, 20 * 60),
                bossBarTitle,
                bossBarDamageText,
                bossBarCriticalDamageText
        );

        IndicatorSettings indicators = new IndicatorSettings(
                new TextStyle(
                        config.getBoolean("indicators.damage.enabled", true),
                        validIndicatorTemplate(config.getString("indicators.damage.text", DEFAULT_DAMAGE_TEXT), DEFAULT_DAMAGE_TEXT, "indicators.damage.text", decimalPlaces, logger)
                ),
                new TextStyle(
                        config.getBoolean("indicators.critical.enabled", true),
                        validIndicatorTemplate(config.getString("indicators.critical.text", DEFAULT_CRITICAL_TEXT), DEFAULT_CRITICAL_TEXT, "indicators.critical.text", decimalPlaces, logger)
                ),
                new TextStyle(
                        config.getBoolean("indicators.healing.enabled", true),
                        validIndicatorTemplate(config.getString("indicators.healing.text", DEFAULT_HEALING_TEXT), DEFAULT_HEALING_TEXT, "indicators.healing.text", decimalPlaces, logger)
                ),
                boundedInt(config, logger, "indicators.duration-ticks", 20, 1, 20 * 60),
                boundedDouble(config, logger, "indicators.vertical-offset", 0.35, -2.0, 5.0),
                boundedDouble(config, logger, "indicators.random-horizontal-offset", 0.35, 0.0, 3.0),
                boundedDouble(config, logger, "indicators.rise-per-tick", 0.045, -0.5, 0.5),
                (float) boundedDouble(config, logger, "indicators.view-range", 1.0, 0.1, 4.0)
        );

        return new CombatDisplayConfig(decimalPlaces, bossBar, indicators);
    }

    private static String validBossBarTemplate(
            String value,
            String fallback,
            String damageText,
            int decimalPlaces,
            Logger logger
    ) {
        try {
            DisplayText.bossBar(
                    value,
                    Component.text("Цель"),
                    10.0,
                    20.0,
                    DisplayText.indicator(damageText, 2.5, decimalPlaces),
                    decimalPlaces
            );
            return value;
        } catch (RuntimeException exception) {
            logger.warning("Некорректный MiniMessage в bossbar.title. Используется оформление по умолчанию: " + exception.getMessage());
            return fallback;
        }
    }

    private static String validIndicatorTemplate(
            String value,
            String fallback,
            String path,
            int decimalPlaces,
            Logger logger
    ) {
        try {
            DisplayText.indicator(value, 2.5, decimalPlaces);
            return value;
        } catch (RuntimeException exception) {
            logger.warning("Некорректный MiniMessage в " + path + ". Используется оформление по умолчанию: " + exception.getMessage());
            return fallback;
        }
    }

    private static int boundedInt(FileConfiguration config, Logger logger, String path, int fallback, int min, int max) {
        int value = config.getInt(path, fallback);
        if (value < min || value > max) {
            logger.warning(path + " должен быть в диапазоне " + min + ".." + max + ". Используется " + fallback + ".");
            return fallback;
        }
        return value;
    }

    private static double boundedDouble(FileConfiguration config, Logger logger, String path, double fallback, double min, double max) {
        double value = config.getDouble(path, fallback);
        if (!Double.isFinite(value) || value < min || value > max) {
            logger.warning(path + " должен быть в диапазоне " + min + ".." + max + ". Используется " + fallback + ".");
            return fallback;
        }
        return value;
    }

    public record BossBarSettings(
            boolean enabled,
            int durationTicks,
            String title,
            String damageText,
            String criticalDamageText
    ) {
    }

    public record IndicatorSettings(
            TextStyle damage,
            TextStyle critical,
            TextStyle healing,
            int durationTicks,
            double verticalOffset,
            double randomHorizontalOffset,
            double risePerTick,
            float viewRange
    ) {
    }

    public record TextStyle(boolean enabled, String text) {
    }
}
