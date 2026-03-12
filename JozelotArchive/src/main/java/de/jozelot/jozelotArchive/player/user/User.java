package de.jozelot.jozelotArchive.player.user;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class User
{
    private UUID uuid;
    //Settings setting;

    public User()
    {

    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }
}
