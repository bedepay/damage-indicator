package ru.dimas.damageindicator;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.dimas.damageindicator.command.DamageIndicatorCommand;
import ru.dimas.damageindicator.config.CombatDisplayConfig;
import ru.dimas.damageindicator.listener.CombatListener;
import ru.dimas.damageindicator.service.BossBarService;
import ru.dimas.damageindicator.service.FloatingIndicatorService;

public final class DamageIndicatorPlugin extends JavaPlugin {

    private CombatDisplayConfig displayConfig;
    private BossBarService bossBarService;
    private FloatingIndicatorService floatingIndicatorService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadPluginConfiguration();

        bossBarService = new BossBarService(this);
        floatingIndicatorService = new FloatingIndicatorService(this);

        getServer().getPluginManager().registerEvents(
                new CombatListener(this, bossBarService, floatingIndicatorService),
                this
        );

        PluginCommand command = getCommand("damageindicator");
        if (command == null) {
            throw new IllegalStateException("Команда damageindicator отсутствует в plugin.yml");
        }
        DamageIndicatorCommand executor = new DamageIndicatorCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        getLogger().info("DamageIndicator 2.0 включён для Paper 1.21.11.");
    }

    @Override
    public void onDisable() {
        if (bossBarService != null) {
            bossBarService.shutdown();
        }
        if (floatingIndicatorService != null) {
            floatingIndicatorService.shutdown();
        }
    }

    public void reloadPluginConfiguration() {
        reloadConfig();
        displayConfig = CombatDisplayConfig.load(getConfig(), getLogger());
    }

    public CombatDisplayConfig displayConfig() {
        return displayConfig;
    }
}
