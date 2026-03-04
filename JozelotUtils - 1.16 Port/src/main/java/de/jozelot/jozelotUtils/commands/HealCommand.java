package de.jozelot.jozelotUtils.commands;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HealCommand implements CommandExecutor {

    private final JozelotUtils plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public HealCommand(JozelotUtils plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
    }

    // Ersetze die Logik in HealCommand.java:
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("network.utils.command.heal")) {
            sendMessage(sender, lang.format("no-permission", null));
            playSound(sender, "error");
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sendMessage(sender, lang.format("only-player", null));
                return true;
            }
            target = player;
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sendMessage(sender, lang.format("player-not-found", Map.of("player-name", args[0])));
                playSound(sender, "error");
                return true;
            }
        }

        // Heilungs-Logik
        if (target.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }
        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.setFireTicks(0);
        target.getActivePotionEffects().forEach(effect -> target.removePotionEffect(effect.getType()));

        // Feedback via Wrapper
        if (target == sender) {
            sendActionBar(target, lang.format("command-heal-success", null));
        } else {
            sendActionBar(sender, lang.format("command-heal-others-success", Map.of("target-name", target.getName())));
            sendActionBar(target, lang.format("command-heal-success", null));
        }

        playSound(sender, "pling");
        return true;
    }

    private void sendMessage(CommandSender sender, String message) {
        JozelotUtils.adventure().sender(sender).sendMessage(mm.deserialize(message));
    }

    private void sendActionBar(CommandSender sender, String message) {
        JozelotUtils.adventure().sender(sender).sendActionBar(mm.deserialize(message));
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