package de.jozelot.jozelotLobby.ui.items;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.ui.inventories.InventoryType;
import de.jozelot.jozelotLobby.ui.inventories.navigation.NavigatorMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ClickHandler implements Listener {

    private final JozelotLobby plugin;

    public ClickHandler(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        Player player = event.getPlayer();

        if (itemStack == null || !itemStack.hasItemMeta() || player.hasCooldown(itemStack.getType())) return;

        ItemMeta itemMeta = itemStack.getItemMeta();
        String itemId = itemMeta.getPersistentDataContainer().get(HotbarItems.ITEM_ID, PersistentDataType.STRING);

        if (itemId == null) return;

        LobbyPlayer lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        if (lobbyPlayer == null) {
            event.setCancelled(true);
            plugin.getLogger().info("Kein LobbyPlayer Objekt für " + player.getName());
            return;
        }

        switch (itemId) {
            case "navigator":
                lobbyPlayer.openInventory(InventoryType.NAVIGATOR);
                break;
            case "profile":

                break;
            case "player_hider":
                lobbyPlayer.toggleHider();
                plugin.getHotbarManager().giveItems(player);
                player.setCooldown(itemStack, 20);
                break;
        }

        lobbyPlayer.playSound("pling");

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack itemStack = event.getCurrentItem();
        Inventory inventory = event.getClickedInventory();
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        boolean isProtected = itemMeta.getPersistentDataContainer().get(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN);

        if (isProtected) {
            event.setCancelled(true);
        }

        String itemId = itemMeta.getPersistentDataContainer().get(HotbarItems.ITEM_ID, PersistentDataType.STRING);

        if (itemId == null) return;

        LobbyPlayer lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        switch (itemId) {
            case "spawn_button":
                player.performCommand("spawn");
                player.closeInventory();
                return;
            case "back_button":
                if (inventory.getHolder() instanceof NavigatorMenu) {
                    player.closeInventory();
                }
                break;
        }

        lobbyPlayer.playSound("pling");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (inventory.getHolder() instanceof NavigatorMenu menu) {
            menu.stopUpdateTask();
        }
    }
}
