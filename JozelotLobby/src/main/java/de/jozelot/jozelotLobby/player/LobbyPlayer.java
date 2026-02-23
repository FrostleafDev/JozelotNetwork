package de.jozelot.jozelotLobby.player;

import de.jozelot.jozelotLobby.items.HotbarItems;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LobbyPlayer {

    private final UUID uuid;
    private HotbarItems.HiderState hiderState;

    public LobbyPlayer(UUID uuid, HotbarItems.HiderState hiderState) {
        this.hiderState = hiderState;
        this.uuid = uuid;
    }

    public void setHiderState(HotbarItems.HiderState hiderState) {
        this.hiderState = hiderState;
    }

    public HotbarItems.HiderState getHiderState() {
        return hiderState;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }
}
