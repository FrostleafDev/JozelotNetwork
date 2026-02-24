package de.jozelot.jozelotLobby.player;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.ui.items.HiderState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class LobbyPlayerManager {

    private final JozelotLobby plugin;
    private final LobbyPlayerDatabase lpd;
    private final HashMap<UUID, LobbyPlayer> players = new HashMap<>();

    public LobbyPlayerManager(JozelotLobby plugin) {
        this.plugin = plugin;
        this.lpd = plugin.getLobbyPlayerDatabase();
    }

    /**
     * Erstellt das LobbyPlayer Objekt für Einstellungen usw
     * @param player
     * @return
     */
    public LobbyPlayer createPlayer(Player player) {

        LobbyPlayer lobbyPlayer = new LobbyPlayer(player.getUniqueId(), lpd.getHiderState(player), plugin);
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

    public Collection<LobbyPlayer> getAllPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    public void removeAllPlayers() {
        players.clear();
    }

    public void registerAllPlayers() {

        Collection<UUID> allUUIDs = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).toList();

        Map<UUID, HiderState> states = lpd.loadMultipleHiderStates(allUUIDs);

        states.forEach((uuid, hiderState) -> {
            LobbyPlayer lobbyPlayer = new LobbyPlayer(uuid, hiderState, plugin);
            players.put(uuid, lobbyPlayer);
        });
    }
}
