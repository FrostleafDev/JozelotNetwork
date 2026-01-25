package de.jozelot.jozelotUtils.commands;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class FlyCommand implements CommandExecutor {

    private final ConfigManager config;
    private final LangManager lang;

    private MiniMessage mm = MiniMessage.miniMessage();

    public FlyCommand(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("network.utils.command.fly")) {
            sender.sendMessage(mm.deserialize(lang.format("no-permission", null)));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            if (!sender.hasPermission("network.utils.command.fly.all")) {
                sender.sendMessage(mm.deserialize(lang.format("no-permission", null)));
                return true;
            }

            for (Player all : Bukkit.getOnlinePlayers()) {
                toggleFly(all);
            }

            sender.sendMessage(mm.deserialize(lang.format("command-fly-all-success", null)));
            return true;
        }

        if (args.length == 1) {
            if (!sender.hasPermission("network.utils.command.fly.others")) {
                sender.sendMessage(mm.deserialize(lang.format("no-permission", null)));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(mm.deserialize(lang.format("player-not-found", Map.of("player-name", args[0]))));
                return true;
            }

            boolean newState = toggleFly(target);
            String stateName = newState ? "aktiviert" : "deaktiviert";

            sender.sendMessage(mm.deserialize(lang.format("command-fly-others-success",
                    Map.of("player", target.getName(), "state", stateName))));

            target.sendMessage(mm.deserialize(lang.format("command-fly-success", Map.of("state", stateName))));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize(lang.format("only-player", null)));
            return true;
        }

        boolean newState = toggleFly(player);
        String stateName = newState ? "aktiviert" : "deaktiviert";
        player.sendMessage(mm.deserialize(lang.format("command-fly-success", Map.of("state", stateName))));

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
}