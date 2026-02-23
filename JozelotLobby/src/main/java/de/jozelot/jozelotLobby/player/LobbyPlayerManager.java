package de.jozelot.jozelotLobby.player;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.items.HiderState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class LobbyPlayerManager {

    private final JozelotLobby plugin;
    private final HashMap<UUID, LobbyPlayer> players = new HashMap<>();

    public LobbyPlayerManager(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    /**
     * Erstellt das LobbyPlayer Objekt für Einstellungen usw
     * @param player
     * @return
     */
    public LobbyPlayer createPlayer(Player player) {
        // HIER AUS DATENBANK LADEN WELCHEN STATUS SPIELER HAT

        LobbyPlayer lobbyPlayer = new LobbyPlayer(player.getUniqueId(), HiderState.VISIBLE, plugin);
        players.put(player.getUniqueId(), lobbyPlayer);

        return lobbyPlayer;
    }

    public LobbyPlayer getPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        return  players.get(uuid);
    }

    public LobbyPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public void removePlayer(LobbyPlayer lobbyPlayer) {
        if (lobbyPlayer != null) {
            players.remove(lobbyPlayer.getUuid());
        }
    }
    
    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public void removeAllPlayers() {
        players.clear();
    }

    public void registerAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::createPlayer);
    }
}
