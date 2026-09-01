package ru.dimas.damageindicator.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public final class DisplayText {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private DisplayText() {
    }

    public static Component bossBar(
            String template,
            Component targetName,
            double health,
            double maxHealth,
            double damage,
            Component criticalLabel,
            int decimalPlaces
    ) {
        return MINI_MESSAGE.deserialize(
                template,
                Placeholder.component("target", targetName),
                Placeholder.unparsed("health", DamageMath.format(health, decimalPlaces)),
                Placeholder.unparsed("max_health", DamageMath.format(maxHealth, decimalPlaces)),
                Placeholder.unparsed("damage", DamageMath.format(damage, decimalPlaces)),
                Placeholder.component("critical", criticalLabel)
        );
    }

    public static Component indicator(String template, double amount, int decimalPlaces) {
        return MINI_MESSAGE.deserialize(
                template,
                Placeholder.unparsed("amount", DamageMath.format(amount, decimalPlaces))
        );
    }

    public static Component miniMessage(String template) {
        return MINI_MESSAGE.deserialize(template);
    }
}
