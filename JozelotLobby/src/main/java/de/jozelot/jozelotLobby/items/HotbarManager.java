package de.jozelot.jozelotLobby.items;

import de.jozelot.jozelotLobby.JozelotLobby;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HotbarManager implements Listener {

    private final JozelotLobby plugin;
    private final HotbarItems hotbarItems;

    private ItemStack navigatorItem;
    private ItemStack playerHider;

    public HotbarManager(JozelotLobby plugin) {
        this.plugin = plugin;
        this.hotbarItems = plugin.getHotbarItems();

        loadItems();
    }

    public void loadItems() {
        navigatorItem = plugin.getHotbarItems().getNavigator();
        playerHider = plugin.getHotbarItems().getPlayerHider(HotbarItems.HiderState.VISIBLE);
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

        if (navigatorSlot >= 0 && navigatorSlot <= 8) player.getInventory().setItem(navigatorSlot, navigatorItem);
        if (playerHiderSlot >= 0 && playerHiderSlot <= 8) player.getInventory().setItem(playerHiderSlot, playerHider);
        if (profileSlot >= 0 && profileSlot <= 8) player.getInventory().setItem(profileSlot, plugin.getHotbarItems().getProfil(player));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        clearHotbar(player);
        giveItems(player);
    }

    public void handleReload() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            clearHotbar(p);
            giveItems(p);
        });
    }
}
