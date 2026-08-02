package de.jozelot.jozelotUtils.commands;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HealCommand implements TabExecutor {

    private final JozelotUtils plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public HealCommand(JozelotUtils plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("network.utils.command.heal")) {
            sender.sendMessage(mm.deserialize(lang.format("no-permission", null)));
            playSound(sender, "error");
            return true;
        }

        Player target;

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(mm.deserialize(lang.format("only-player", null)));
                return true;
            }
            target = player;
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(mm.deserialize(lang.format("player-not-found", Map.of("player-name", args[0]))));
                playSound(sender, "error");
                return true;
            }
        }

        if (target.getAttribute(Attribute.MAX_HEALTH) != null) {
            target.setHealth(target.getAttribute(Attribute.MAX_HEALTH).getValue());
        }
        target.setFoodLevel(20);
        target.setSaturation(20f);
        target.setFireTicks(0);
        target.getActivePotionEffects().forEach(effect -> target.removePotionEffect(effect.getType()));

        // Feedback
        if (target == sender) {
            target.sendActionBar(mm.deserialize(lang.format("command-heal-success", null)));
        } else {
            sender.sendActionBar(mm.deserialize(lang.format("command-heal-others-success", Map.of("target-name", target.getName()))));
            target.sendActionBar(mm.deserialize(lang.format("command-heal-success", null)));
        }

        playSound(sender, "pling");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return new ArrayList<>();
    }

    private void playSound(CommandSender sender, String soundKey) {
        if (!(sender instanceof Player player)) return;
        String path = lang.getRaw("sounds." + soundKey);
        if (path != null && !path.isEmpty()) {
            try {
                String cleanedPath = path.trim().toLowerCase().contains(":") ? path.trim().toLowerCase() : "minecraft:" + path.trim().toLowerCase();
                player.playSound(Sound.sound(Key.key(cleanedPath), Sound.Source.UI, 1.0f, 1.0f), Sound.Emitter.self());
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§cUngültiger Sound-Key in lang.yml: " + path);
            }
        }
    }
}