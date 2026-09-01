package ru.dimas.damageindicator.service;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.TextDisplay;
import ru.dimas.damageindicator.DamageIndicatorPlugin;
import ru.dimas.damageindicator.config.CombatDisplayConfig;
import ru.dimas.damageindicator.util.DisplayText;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public final class FloatingIndicatorService {

    private final DamageIndicatorPlugin plugin;
    private final Map<UUID, TextDisplay> activeDisplays = new ConcurrentHashMap<>();

    public FloatingIndicatorService(DamageIndicatorPlugin plugin) {
        this.plugin = plugin;
    }

    public void showDamage(LivingEntity target, double amount, boolean critical) {
        CombatDisplayConfig.IndicatorSettings settings = plugin.displayConfig().indicators();
        CombatDisplayConfig.TextStyle style = critical ? settings.critical() : settings.damage();
        spawn(target, amount, style, settings);
    }

    public void showHealing(LivingEntity target, double amount) {
        CombatDisplayConfig.IndicatorSettings settings = plugin.displayConfig().indicators();
        spawn(target, amount, settings.healing(), settings);
    }

    public void shutdown() {
        for (TextDisplay display : activeDisplays.values()) {
            if (display.isValid()) {
                display.remove();
            }
        }
        activeDisplays.clear();
    }

    private void spawn(
            LivingEntity target,
            double amount,
            CombatDisplayConfig.TextStyle style,
            CombatDisplayConfig.IndicatorSettings settings
    ) {
        if (!style.enabled()) {
            return;
        }

        Location location = target.getLocation().add(
                randomOffset(settings.randomHorizontalOffset()),
                target.getHeight() + settings.verticalOffset(),
                randomOffset(settings.randomHorizontalOffset())
        );
        TextDisplay display = target.getWorld().spawn(location, TextDisplay.class, entity -> {
            entity.text(DisplayText.indicator(style.text(), amount, plugin.displayConfig().decimalPlaces()));
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
            entity.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
            entity.setDefaultBackground(false);
            entity.setShadowed(true);
            entity.setSeeThrough(true);
            entity.setLineWidth(160);
            entity.setTeleportDuration(1);
            entity.setInterpolationDuration(1);
            entity.setViewRange(settings.viewRange());
            entity.setPersistent(false);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            entity.setSilent(true);
        });

        activeDisplays.put(display.getUniqueId(), display);
        AtomicInteger age = new AtomicInteger();
        display.getScheduler().runAtFixedRate(
                plugin,
                task -> {
                    if (age.incrementAndGet() >= settings.durationTicks()) {
                        task.cancel();
                        remove(display);
                        return;
                    }
                    display.teleport(display.getLocation().add(0.0, settings.risePerTick(), 0.0));
                },
                () -> activeDisplays.remove(display.getUniqueId()),
                1L,
                1L
        );
    }

    private void remove(TextDisplay display) {
        activeDisplays.remove(display.getUniqueId());
        if (display.isValid()) {
            display.remove();
        }
    }

    private double randomOffset(double maximum) {
        return maximum == 0.0
                ? 0.0
                : ThreadLocalRandom.current().nextDouble(-maximum, maximum);
    }
}
