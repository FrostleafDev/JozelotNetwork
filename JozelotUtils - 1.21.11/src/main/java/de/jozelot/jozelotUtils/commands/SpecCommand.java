package de.jozelot.jozelotUtils.commands;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import de.jozelot.jozelotUtils.utils.ConsoleLogger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SpecCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager config;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final Map<UUID, GameMode> previousGameModes = new HashMap<>();

    public SpecCommand(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
        this.consoleLogger = plugin.getConsoleLogger();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("network.utils.command.spec")) {
            sender.sendMessage(mm.deserialize(lang.format("no-permission", null)));
            playSound(sender, "error");
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("network.utils.command.spec.others")) {
                sender.sendMessage(mm.deserialize(lang.format("no-permission", null)));
                playSound(sender, "error");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(mm.deserialize(lang.format("player-not-found", Map.of("player-name", args[0]))));
                playSound(sender, "error");
                return true;
            }
            toggleSpectator(target, sender);
            playSound(sender, "pling");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize(lang.format("only-player", null)));
            return true;
        }
        toggleSpectator(player, player);
        playSound(sender, "pling");
        return true;
    }

    private void toggleSpectator(Player target, CommandSender executor) {
        String executorName = (executor instanceof Player p) ? p.getName() : "Konsole";

        if (target.getGameMode() != GameMode.SPECTATOR) {
            previousGameModes.put(target.getUniqueId(), target.getGameMode());
            target.setGameMode(GameMode.SPECTATOR);

            target.sendActionBar(mm.deserialize(lang.format("command-spec-success", null)));
            if (!target.equals(executor)) {
                executor.sendMessage(mm.deserialize(lang.format("command-spec-others-success", Map.of("target", target.getName()))));
            }

            logAndNotify(executor, target, "aktiviert");
        } else {
            GameMode backTo = previousGameModes.remove(target.getUniqueId());

            if (backTo == null) {
                try {
                    backTo = GameMode.valueOf(config.getDefaultGamemode().toUpperCase());
                } catch (Exception e) {
                    backTo = GameMode.SURVIVAL;
                }
            }

            target.setGameMode(backTo);
            String gmName = backTo.name().toLowerCase();
            String formattedName = gmName.substring(0, 1).toUpperCase() + gmName.substring(1);

            target.sendActionBar(mm.deserialize(lang.format("command-spec-back", Map.of("gm", formattedName))));

            logAndNotify(executor, target, "deaktiviert (zurück zu " + backTo.name() + ")");
        }
    }

    private void logAndNotify(CommandSender sender, Player target, String state) {
        String name = (sender instanceof Player p) ? p.getName() : "Konsole";

        consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + name +
                "<" + config.getColorPrimary() + "> hat Spectator für " + target.getName() + " " + state);

        String langKey = (sender.equals(target)) ? "command-spec-success-admin" : "command-spec-others-success-admin";
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("network.get.logs") && !p.equals(sender)) {
                p.sendMessage(mm.deserialize(lang.format(langKey, Map.of(
                        "player-name", name,
                        "target-name", target.getName(),
                        "state", state))));
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (!player.hasPermission("network.utils.command.spec.other")) {
            return List.of();
        }

        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (args.length == 1) {

            List<String> suggestions = new ArrayList<>();

            for (Player p : Bukkit.getOnlinePlayers()) {
                suggestions.add(p.getName());
            }

            return suggestions.stream()
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .sorted()
                    .toList();
        }
        return List.of();
    }

    private void playSound(CommandSender sender, String soundKey) {
        if (!(sender instanceof Player player)) return;

        String path = lang.getRaw("sounds." + soundKey);

        if (path != null && !path.isEmpty()) {
            try {
                String cleanedPath = path.trim().toLowerCase();

                if (!cleanedPath.contains(":")) {
                    cleanedPath = "minecraft:" + cleanedPath;
                }

                Sound sound = Sound.sound(
                        Key.key(cleanedPath),
                        Sound.Source.UI,
                        1.0f,
                        1.0f
                );
                player.playSound(sound, Sound.Emitter.self());
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§cUngültiger Sound-Key in lang.yml: " + path);
            }
        }
    }
}