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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("network.utils.command.flyspeed")) {
            sender.sendMessage(mm.deserialize(lang.format("no-permission", null)));
            playSound(sender, "error");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize(lang.format("only-player", null)));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(mm.deserialize(lang.format("command-flyspeed-usage", null)));
            playSound(sender, "error");
            return true;
        }

        try {
            float input = Float.parseFloat(args[0]);

            float targetSpeed = input / 10.0f;

            if (targetSpeed > 1.0f) targetSpeed = 1.0f;
            if (targetSpeed < -1.0f) targetSpeed = -1.0f;

            player.setFlySpeed(targetSpeed);

            player.sendMessage(mm.deserialize(lang.format("command-flyspeed-success",
                    Map.of("speed", String.valueOf(input)))));

            playSound(sender, "pling");
        } catch (NumberFormatException e) {
            player.sendMessage(mm.deserialize(lang.format("invalid-number", Map.of("input", args[0]))));

            playSound(sender, "error");
        }
        return true;
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
