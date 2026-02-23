package de.jozelot.jozelotLobby.player;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.items.HotbarItems;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class LobbyPlayerManager {

    private final JozelotLobby plugin;
    private final HashMap<UUID, LobbyPlayer> players = new HashMap<>();

    public LobbyPlayerManager(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    public void createPlayer(Player player) {
        players.put(player.getUniqueId(), new LobbyPlayer(player.getUniqueId(), HotbarItems.HiderState.VISIBLE));
    }
}
