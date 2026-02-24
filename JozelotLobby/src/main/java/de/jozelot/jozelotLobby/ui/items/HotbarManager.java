package de.jozelot.jozelotLobby.ui.items;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.player.LobbyPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HotbarManager{

    private final JozelotLobby plugin;
    private final HotbarItems hotbarItems;
    private final LobbyPlayerManager lobbyPlayerManager;

    public HotbarManager(JozelotLobby plugin) {
        this.plugin = plugin;
        this.hotbarItems = plugin.getHotbarItems();
        this.lobbyPlayerManager = plugin.getLobbyPlayerManager();

    }

    public void clearHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, null);
        }
    }

    public void giveItems(Player player) {
        int navigatorSlot = plugin.getConfig().getInt("items.navigator.slot");
        int playerHiderSlot = plugin.getConfig().getInt("items.player_hider.slot");
        int profileSlot = plugin.getConfig().getInt("items.profile.slot");

        HiderState hiderState = lobbyPlayerManager.getPlayer(player).getHiderState();

        if (navigatorSlot >= 0 && navigatorSlot <= 8) player.getInventory().setItem(navigatorSlot, plugin.getHotbarItems().getNavigator());
        if (playerHiderSlot >= 0 && playerHiderSlot <= 8) player.getInventory().setItem(playerHiderSlot, plugin.getHotbarItems().getPlayerHider(hiderState));
        if (profileSlot >= 0 && profileSlot <= 8) player.getInventory().setItem(profileSlot, plugin.getHotbarItems().getProfile(player));
    }

    public void handleReload() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            clearHotbar(p);
            giveItems(p);
            lobbyPlayerManager.getAllPlayers().forEach(LobbyPlayer::updateVisibility);
        });
    }
}
