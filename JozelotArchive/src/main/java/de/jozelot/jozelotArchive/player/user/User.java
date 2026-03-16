package de.jozelot.jozelotArchive.player.user;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.InventoryType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class User
{
    private final UUID uuid;
    private final JozelotArchive plugin;
    //Settings setting;

    public User(UUID uuid, JozelotArchive plugin) {
        this.uuid = uuid;
        this.plugin = plugin;
    }

    public void openInventory(InventoryType inventory) {

    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }
}
