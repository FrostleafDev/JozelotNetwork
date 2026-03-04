package de.jozelot.jozelotUtils.commands;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FlySpeedCommand implements CommandExecutor {

    private final ConfigManager config;
    private final LangManager lang;

    private MiniMessage mm = MiniMessage.miniMessage();

    public FlySpeedCommand(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
    }

    // Ersetze die onCommand und playSound in FlySpeedCommand.java:
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("network.utils.command.flyspeed")) {
            sendMessage(sender, lang.format("no-permission", null));
            playSound(sender, "error");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sendMessage(sender, lang.format("only-player", null));
            return true;
        }

        if (args.length < 1) {
            sendMessage(player, lang.format("command-flyspeed-usage", null));
            playSound(sender, "error");
            return true;
        }

        try {
            float input = Float.parseFloat(args[0]);
            float targetSpeed = input / 10.0f;

            if (targetSpeed > 1.0f) targetSpeed = 1.0f;
            if (targetSpeed < -1.0f) targetSpeed = -1.0f;

            player.setFlySpeed(targetSpeed);
            sendMessage(player, lang.format("command-flyspeed-success", Map.of("speed", String.valueOf(input))));
            playSound(sender, "pling");
        } catch (NumberFormatException e) {
            sendMessage(player, lang.format("invalid-number", Map.of("input", args[0])));
            playSound(sender, "error");
        }
        return true;
    }

    private void sendMessage(CommandSender sender, String message) {
        JozelotUtils.adventure().sender(sender).sendMessage(mm.deserialize(message));
    }

    private void playSound(CommandSender sender, String soundKey) {
        if (!(sender instanceof Player player)) return;
        String path = lang.getRaw("sounds." + soundKey);
        if (path == null || path.isEmpty()) return;
        try {
            Sound sound = Sound.sound(Key.key(path.contains(":") ? path : "minecraft:" + path), Sound.Source.MASTER, 1.0f, 1.0f);
            JozelotUtils.adventure().player(player).playSound(sound);
        } catch (Exception ignored) {}
    }
}
