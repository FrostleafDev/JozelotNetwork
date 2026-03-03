package de.jozelot.jozelotLobby.commands;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.ui.inventories.InventoryType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class OpenMenu implements CommandExecutor {

    private final JozelotLobby plugin;

    public OpenMenu(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        String fullCommand = label + " " + String.join(" ", args);
        if (args.length != 1) {
            sendBlocked(player, fullCommand);
            return true;
        }
        String menuName = args[0];

        if (menuName.isEmpty() || menuName.isBlank()) {
            sendBlocked(player, fullCommand);
            return true;
        }
        LobbyPlayer lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        try {
            InventoryType type = InventoryType.valueOf(menuName.toUpperCase());
            lobbyPlayer.openInventory(type);
            lobbyPlayer.playSound("pling");
        } catch (IllegalArgumentException e) {
            sendBlocked(player, fullCommand);
        }
        return true;
    }

    private void sendBlocked(Player player, String fullCommand) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getLang().format("blocked-command", Map.of("command", fullCommand))));
    }
}
