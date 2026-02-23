package de.jozelot.jozelotLobby.player;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.items.HotbarManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final JozelotLobby plugin;
    private final HotbarManager hotbarManager;
    private final LobbyPlayerManager lobbyPlayerManager;

    public PlayerConnectionListener(JozelotLobby plugin) {
        this.plugin = plugin;
        this.hotbarManager = plugin.getHotbarManager();
        this.lobbyPlayerManager = plugin.getLobbyPlayerManager();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        LobbyPlayer lobbyPlayer = lobbyPlayerManager.getPlayer(player);

        if (lobbyPlayer == null) {
            lobbyPlayer = lobbyPlayerManager.createPlayer(player);
        }

        hotbarManager.clearHotbar(player);
        hotbarManager.giveItems(player);

        lobbyPlayer.updateVisibility();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;

            LobbyPlayer onlineLobbyPlayer = lobbyPlayerManager.getPlayer(onlinePlayer);

            if (onlineLobbyPlayer != null) {
                onlineLobbyPlayer.updateVisibility();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        lobbyPlayerManager.removePlayer(player);
    }
}
