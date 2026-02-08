package de.jozelot.jozelotUtils.commands;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import de.jozelot.jozelotUtils.utils.ConsoleLogger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class FlyCommand implements CommandExecutor {

    private final ConfigManager config;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;

    private MiniMessage mm = MiniMessage.miniMessage();

    public FlyCommand(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
        this.consoleLogger = plugin.getConsoleLogger();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("network.utils.command.fly")) {
            sender.sendMessage(mm.deserialize(lang.format("no-permission", null)));
            playSound(sender, "error");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            if (!sender.hasPermission("network.utils.command.fly.all")) {
                sender.sendMessage(mm.deserialize(lang.format("no-permission", null)));
                playSound(sender, "error");
                return true;
            }

            for (Player all : Bukkit.getOnlinePlayers()) {
                toggleFly(all);
                playSound(sender, "error");
            }

            sender.sendActionBar(mm.deserialize(lang.format("command-fly-all-success",null)));

            String name = (sender instanceof Player player) ? player.getName() : "Konsole";
            consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + name + "<" + config.getColorPrimary() + "> hat den Flugmodus für alle Spieler umgeschaltet");
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("network.get.logs") && !player.equals(sender)) {
                    player.sendMessage(mm.deserialize(lang.format("command-fly-all-success-admin", Map.of("player-name", name))));
                }
            }

            playSound(sender, "pling");

            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("network.utils.command.fly.others")) {
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

            boolean newState = toggleFly(target);
            String stateName = newState ? "aktiviert" : "deaktiviert";

            sender.sendMessage(mm.deserialize(lang.format("command-fly-others-success",
                    Map.of("player", target.getName(), "state", stateName))));

            playSound(sender, "pling");

            target.sendActionBar(mm.deserialize(lang.format("command-fly-success", Map.of("state", stateName))));
            playSound(target, "pling");

            sender.sendActionBar(mm.deserialize(lang.format("command-fly-all-success",null)));

            String name = (sender instanceof Player player) ? player.getName() : "Konsole";
            consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + name + "<" + config.getColorPrimary() + "> hat den Flugmodus für " + args[0] + stateName);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("network.get.logs") && !player.equals(sender)) {
                    player.sendMessage(mm.deserialize(lang.format("command-fly-others-success-admin", Map.of("player-name", name, "target-name", args[0], "state", stateName))));
                }
            }

            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize(lang.format("only-player", null)));
            return true;
        }

        boolean newState = toggleFly(player);
        String stateName = newState ? "aktiviert" : "deaktiviert";
        player.sendActionBar(mm.deserialize(lang.format("command-fly-success", Map.of("state", stateName))));

        String name = (sender instanceof Player) ? player.getName() : "Konsole";
        consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + name + "<" + config.getColorPrimary() + "> hat den Flugmodus für sich " + stateName);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("network.get.logs") && !p.equals(player)) {
                p.sendMessage(mm.deserialize(lang.format("command-fly-success-admin", Map.of("player-name", name, "state", stateName))));
            }
        }

        playSound(sender, "pling");

        return true;
    }

    private boolean toggleFly(Player player) {
        boolean newState = !player.getAllowFlight();

        if (newState) {
            player.setAllowFlight(true);
            player.setFlying(true);
            if (player.isOnGround()) {
                player.teleport(player.getLocation().add(0, 0.1, 0));
            }
        } else {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
        return newState;
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