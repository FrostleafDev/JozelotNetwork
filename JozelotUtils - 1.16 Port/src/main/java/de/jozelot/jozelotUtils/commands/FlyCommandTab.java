package de.jozelot.jozelotUtils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FlyCommandTab implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (!player.hasPermission("network.utils.command.fly")) {
            return List.of();
        }

        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (args.length == 1) {

            List<String> suggestions = new ArrayList<>();

            for (Player p : Bukkit.getOnlinePlayers()) {
                suggestions.add(p.getName());
            }
            suggestions.add("all");

            return suggestions.stream()
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .sorted()
                    .toList();
        }
        return List.of();
    }
}
