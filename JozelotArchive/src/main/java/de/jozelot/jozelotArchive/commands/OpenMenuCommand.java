package de.jozelot.jozelotArchive.commands;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.menus.InventoryType;
import de.jozelot.jozelotArchive.player.user.User;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class OpenMenuCommand implements CommandExecutor {

    private JozelotArchive plugin;

    public OpenMenuCommand(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        String fullCommand = label + " " + String.join(" ", args);

        if (args.length < 1) {
            sendBlocked(player, fullCommand);
            return true;
        }

        String menuTitle = args[0].toUpperCase();
        InventoryType type;

        try {
            type = InventoryType.valueOf(menuTitle);
        } catch (IllegalArgumentException e) {
            sendBlocked(player, fullCommand);
            return true;
        }

        User user = plugin.getServiceManager().getUserManager().getUser(player);

        user.openInventory(type);
        return true;
    }

    private void sendBlocked(Player player, String fullCommand) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getServiceManager().getLangManager().format("blocked-command", Map.of("command", fullCommand))));
    }
}
