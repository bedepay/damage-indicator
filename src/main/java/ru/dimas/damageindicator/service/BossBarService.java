package ru.dimas.damageindicator.service;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import ru.dimas.damageindicator.DamageIndicatorPlugin;
import ru.dimas.damageindicator.config.CombatDisplayConfig;
import ru.dimas.damageindicator.util.DamageMath;
import ru.dimas.damageindicator.util.DisplayText;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BossBarService {

    private final DamageIndicatorPlugin plugin;
    private final Map<UUID, ActiveBossBar> activeBars = new ConcurrentHashMap<>();

    public BossBarService(DamageIndicatorPlugin plugin) {
        this.plugin = plugin;
    }

    public void show(
            Player viewer,
            LivingEntity target,
            double remainingHealth,
            double maxHealth,
            double damage,
            boolean critical
    ) {
        CombatDisplayConfig config = plugin.displayConfig();
        CombatDisplayConfig.BossBarSettings settings = config.bossBar();
        if (!settings.enabled()) {
            hide(viewer);
            return;
        }

        float progress = DamageMath.healthProgress(remainingHealth, maxHealth);
        String damageTemplate = critical ? settings.criticalDamageText() : settings.damageText();
        Component damageText = DisplayText.indicator(damageTemplate, damage, config.decimalPlaces());
        Component title = DisplayText.bossBar(
                settings.title(),
                targetName(target),
                remainingHealth,
                maxHealth,
                damageText,
                config.decimalPlaces()
        );

        ActiveBossBar previous = activeBars.remove(viewer.getUniqueId());
        BossBar bossBar;
        if (previous == null) {
            bossBar = BossBar.bossBar(title, progress, colorFor(progress), BossBar.Overlay.PROGRESS);
            viewer.showBossBar(bossBar);
        } else {
            previous.expiry().cancel();
            bossBar = previous.bossBar();
            bossBar.name(title).progress(progress).color(colorFor(progress));
        }

        ScheduledTask expiry = viewer.getScheduler().runDelayed(
                plugin,
                task -> hideIfCurrent(viewer, bossBar, task),
                () -> activeBars.remove(viewer.getUniqueId()),
                settings.durationTicks()
        );
        if (expiry == null) {
            viewer.hideBossBar(bossBar);
            return;
        }
        activeBars.put(viewer.getUniqueId(), new ActiveBossBar(bossBar, expiry));
    }

    public void hide(Player player) {
        ActiveBossBar active = activeBars.remove(player.getUniqueId());
        if (active == null) {
            return;
        }
        active.expiry().cancel();
        player.hideBossBar(active.bossBar());
    }

    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hide(player);
        }
        activeBars.clear();
    }

    private void hideIfCurrent(Player player, BossBar bossBar, ScheduledTask task) {
        ActiveBossBar current = activeBars.get(player.getUniqueId());
        if (current == null || current.expiry() != task) {
            return;
        }
        if (activeBars.remove(player.getUniqueId(), current)) {
            player.hideBossBar(bossBar);
        }
    }

    private Component targetName(LivingEntity target) {
        if (target instanceof Player player) {
            return player.displayName();
        }
        if (target.customName() != null) {
            return target.customName();
        }
        return Component.translatable(target.getType().translationKey());
    }

    private BossBar.Color colorFor(float progress) {
        if (progress > 0.66f) {
            return BossBar.Color.GREEN;
        }
        if (progress > 0.33f) {
            return BossBar.Color.YELLOW;
        }
        return BossBar.Color.RED;
    }

    private record ActiveBossBar(BossBar bossBar, ScheduledTask expiry) {
    }
}
