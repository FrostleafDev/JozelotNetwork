package de.jozelot.jozelotUtils.commands;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpawnCommand extends Command {

    private final ConfigManager config;
    private final LangManager lang;
    private MiniMessage mm = MiniMessage.miniMessage();

    public SpawnCommand(JozelotUtils plugin) {
        super("spawn", "Teleportiert dich zum Spawn", "/spawn", new ArrayList<>());
        this.setPermission("network.utils.command.spawn");

        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player) ){
            sender.sendMessage(mm.deserialize(lang.format("only-player", null)));
            return true;
        }
        if (!config.isSpawnCommand()) {
            sender.sendMessage(mm.deserialize(lang.format("blocked-command", Map.of("command", "/spawn"))));
            return true;
        }
        if (!player.hasPermission("network.utils.command.spawn")) {
            sender.sendMessage(mm.deserialize(lang.format("no-permission", null)));
            playSound(sender, "error");
            return true;
        }

        player.teleport(config.getSpawnLocation());
        player.sendMessage(mm.deserialize(lang.format("command-spawn-success", null)));
        playSound(sender, "success");
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return new ArrayList<>();
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
                        Sound.Source.MASTER,
                        1.0f,
                        1.0f
                );
                player.playSound(sound);
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§cUngültiger Sound-Key in lang.yml: " + path);
            }
        }
    }
}
