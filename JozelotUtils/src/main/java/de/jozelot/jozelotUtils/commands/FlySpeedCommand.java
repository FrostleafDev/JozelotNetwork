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
            if (sender instanceof Player) {
                String soundPath = lang.get("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        sender.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                    }
                }
            }
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize(lang.format("only-player", null)));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(mm.deserialize(lang.format("command-flyspeed-usage", null)));
            if (sender instanceof Player) {
                String soundPath = lang.get("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        sender.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                    }
                }
            }
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

            if (sender instanceof Player) {
                String soundPath = lang.get("sounds.success");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        sender.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                    }
                }
            }

        } catch (NumberFormatException e) {
            player.sendMessage(mm.deserialize(lang.format("invalid-number", Map.of("input", args[0]))));
            if (sender instanceof Player) {
                String soundPath = lang.get("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        sender.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception esa) {
                    }
                }
            }
        }

        return true;
    }
}
