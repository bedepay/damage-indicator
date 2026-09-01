package ru.dimas.damageindicator.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import ru.dimas.damageindicator.DamageIndicatorPlugin;

import java.util.List;
import java.util.Locale;

public final class DamageIndicatorCommand implements TabExecutor {

    private static final String RELOAD_PERMISSION = "damageindicator.reload";

    private final DamageIndicatorPlugin plugin;

    public DamageIndicatorCommand(DamageIndicatorPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(Component.text("Использование: /" + label + " reload", NamedTextColor.YELLOW));
            return true;
        }
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            sender.sendMessage(Component.text("У вас нет прав на эту команду.", NamedTextColor.RED));
            return true;
        }

        plugin.reloadPluginConfiguration();
        sender.sendMessage(Component.text("Конфигурация DamageIndicator перезагружена.", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (args.length == 1
                && sender.hasPermission(RELOAD_PERMISSION)
                && "reload".startsWith(args[0].toLowerCase(Locale.ROOT))) {
            return List.of("reload");
        }
        return List.of();
    }
}
