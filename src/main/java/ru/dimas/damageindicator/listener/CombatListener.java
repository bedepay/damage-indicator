package ru.dimas.damageindicator.listener;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;
import ru.dimas.damageindicator.DamageIndicatorPlugin;
import ru.dimas.damageindicator.service.BossBarService;
import ru.dimas.damageindicator.service.FloatingIndicatorService;
import ru.dimas.damageindicator.util.DamageMath;

public final class CombatListener implements Listener {

    private static final double MINIMUM_VALUE = 0.001;

    private final DamageIndicatorPlugin plugin;
    private final BossBarService bossBars;
    private final FloatingIndicatorService indicators;

    public CombatListener(
            DamageIndicatorPlugin plugin,
            BossBarService bossBars,
            FloatingIndicatorService indicators
    ) {
        this.plugin = plugin;
        this.bossBars = bossBars;
        this.indicators = indicators;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity target) || target instanceof ArmorStand) {
            return;
        }

        double damage = event.getFinalDamage();
        if (!Double.isFinite(damage) || damage < MINIMUM_VALUE) {
            return;
        }

        boolean critical = event.isCritical();
        indicators.showDamage(target, damage, critical);

        Player attacker = resolvePlayer(event.getDamager());
        if (attacker == null) {
            return;
        }

        AttributeInstance maxHealthAttribute = target.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            plugin.getLogger().fine("У сущности " + target.getType() + " отсутствует атрибут MAX_HEALTH.");
            return;
        }

        double maxHealth = maxHealthAttribute.getValue();
        double remainingHealth = DamageMath.remainingHealth(target.getHealth(), damage);
        bossBars.show(attacker, target, remainingHealth, maxHealth, damage, critical);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHealing(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity target) || target instanceof ArmorStand) {
            return;
        }

        AttributeInstance maxHealthAttribute = target.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute == null) {
            return;
        }
        double actualHealing = Math.min(event.getAmount(), maxHealthAttribute.getValue() - target.getHealth());
        if (!Double.isFinite(actualHealing) || actualHealing < MINIMUM_VALUE) {
            return;
        }
        indicators.showHealing(target, actualHealing);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        bossBars.hide(event.getPlayer());
    }

    private Player resolvePlayer(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }
        return null;
    }
}
