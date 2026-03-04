package de.jozelot.jozelotUtils.commands;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import de.jozelot.jozelotUtils.utils.ConsoleLogger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FlyCommand implements CommandExecutor {

    private final JozelotUtils plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public FlyCommand(JozelotUtils plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
        this.consoleLogger = plugin.getConsoleLogger();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("network.utils.command.fly")) {
            sendMessage(sender, lang.format("no-permission", null));
            playSound(sender, "error");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            if (!sender.hasPermission("network.utils.command.fly.all")) {
                sendMessage(sender, lang.format("no-permission", null));
                playSound(sender, "error");
                return true;
            }

            for (Player all : Bukkit.getOnlinePlayers()) {
                toggleFly(all);
            }

            sendActionBar(sender, lang.format("command-fly-all-success", null));

            String name = (sender instanceof Player player) ? player.getName() : "Konsole";
            consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + name + "<" + config.getColorPrimary() + "> hat den Flugmodus für alle Spieler umgeschaltet");

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("network.get.logs") && !player.equals(sender)) {
                    sendMessage(player, lang.format("command-fly-all-success-admin", Map.of("player-name", name)));
                }
            }

            playSound(sender, "pling");
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("network.utils.command.fly.others")) {
                sendMessage(sender, lang.format("no-permission", null));
                playSound(sender, "error");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessage(sender, lang.format("player-not-found", Map.of("player-name", args[0])));
                playSound(sender, "error");
                return true;
            }

            boolean newState = toggleFly(target);
            String stateName = newState ? "aktiviert" : "deaktiviert";

            sendMessage(sender, lang.format("command-fly-others-success", Map.of("player", target.getName(), "state", stateName)));
            playSound(sender, "pling");

            sendActionBar(target, lang.format("command-fly-success", Map.of("state", stateName)));
            playSound(target, "pling");

            String name = (sender instanceof Player player) ? player.getName() : "Konsole";
            consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + name + "<" + config.getColorPrimary() + "> hat den Flugmodus für " + args[0] + " " + stateName);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("network.get.logs") && !player.equals(sender)) {
                    sendMessage(player, lang.format("command-fly-others-success-admin", Map.of("player-name", name, "target-name", args[0], "state", stateName)));
                }
            }
            return true;
        }

        if (!(sender instanceof Player player)) {
            sendMessage(sender, lang.format("only-player", null));
            return true;
        }

        boolean newState = toggleFly(player);
        String stateName = newState ? "aktiviert" : "deaktiviert";
        sendActionBar(player, lang.format("command-fly-success", Map.of("state", stateName)));

        String name = player.getName();
        consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + name + "<" + config.getColorPrimary() + "> hat den Flugmodus für sich " + stateName);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("network.get.logs") && !p.equals(player)) {
                sendMessage(p, lang.format("command-fly-success-admin", Map.of("player-name", name, "state", stateName)));
            }
        }

        playSound(sender, "pling");
        return true;
    }

    private boolean toggleFly(Player player) {
        boolean newState = !player.getAllowFlight();
        player.setAllowFlight(newState);
        player.setFlying(newState);

        if (newState && player.isOnGround()) {
            player.teleport(player.getLocation().add(0, 0.1, 0));
        }
        return newState;
    }

    // Hilfsmethoden für Adventure-Wrapper
    private void sendMessage(CommandSender sender, String message) {
        Component component = mm.deserialize(message);
        JozelotUtils.adventure().sender(sender).sendMessage(component);
    }

    private void sendActionBar(CommandSender sender, String message) {
        Component component = mm.deserialize(message);
        JozelotUtils.adventure().sender(sender).sendActionBar(component);
    }

    // Ersetze die playSound Methode in FlyCommand.java:
    private void playSound(CommandSender sender, String soundKey) {
        if (!(sender instanceof Player player)) return;

        String path = lang.getRaw("sounds." + soundKey);
        if (path == null || path.isEmpty()) return;

        try {
            String cleanedPath = path.trim().toLowerCase();
            if (!cleanedPath.contains(":")) {
                cleanedPath = "minecraft:" + cleanedPath;
            }

            Sound sound = Sound.sound(Key.key(cleanedPath), Sound.Source.MASTER, 1.0f, 1.0f);
            // NUTZE DEN WRAPPER:
            JozelotUtils.adventure().player(player).playSound(sound);
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§cUngültiger Sound-Key in lang.yml: " + path);
        }
    }
}