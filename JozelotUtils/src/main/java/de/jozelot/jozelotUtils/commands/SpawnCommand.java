package de.jozelot.jozelotUtils.commands;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
            return true;
        }

        player.teleport(config.getSpawnLocation());
        player.sendMessage(mm.deserialize(lang.format("command-spawn-success", null)));
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return new ArrayList<>();
    }
}
