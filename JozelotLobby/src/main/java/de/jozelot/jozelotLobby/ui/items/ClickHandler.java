package de.jozelot.jozelotLobby.ui.items;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.player.settings.ColorPreference;
import de.jozelot.jozelotLobby.player.settings.Setting;
import de.jozelot.jozelotLobby.ui.inventories.InventoryType;
import de.jozelot.jozelotLobby.ui.inventories.LobbyInventory;
import de.jozelot.jozelotLobby.ui.inventories.navigation.ArchivMenu;
import de.jozelot.jozelotLobby.ui.inventories.navigation.ChallengeMenu;
import de.jozelot.jozelotLobby.ui.inventories.navigation.NavigatorMenu;
import de.jozelot.jozelotLobby.ui.inventories.profile.SecretMenu;
import de.jozelot.jozelotLobby.ui.inventories.profile.SpielerinfoMenu;
import de.jozelot.jozelotLobby.ui.inventories.profile.settings.ColorPreferenceMenu;
import de.jozelot.jozelotLobby.ui.inventories.profile.settings.SettingsMenu;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class ClickHandler implements Listener {

    private final JozelotLobby plugin;
    private MiniMessage mm = MiniMessage.miniMessage();

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
                lobbyPlayer.openInventory(InventoryType.PROFILE);
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
        if (itemStack == null || !itemStack.hasItemMeta()) return;

        Player player = (Player) event.getWhoClicked();
        ItemMeta itemMeta = itemStack.getItemMeta();

        Boolean isProtected = itemMeta.getPersistentDataContainer().get(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN);
        if (Boolean.TRUE.equals(isProtected)) {
            event.setCancelled(true);
        }


        String itemId = itemMeta.getPersistentDataContainer().get(HotbarItems.ITEM_ID, PersistentDataType.STRING);
        if (itemId == null) return;

        LobbyPlayer lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);
        if (lobbyPlayer == null) return;

        Boolean isOffline = itemMeta.getPersistentDataContainer().get(HotbarItems.IS_OFFLINE, PersistentDataType.BOOLEAN);
        if (Boolean.TRUE.equals(isOffline)) {
            lobbyPlayer.playSound("error");
            player.sendMessage(mm.deserialize(plugin.getLang().format("connect-to-server-offline", null)));
            return;
        }

        if (itemId.startsWith("settings.color.")) {
            String colorId = itemId.replace("settings.color.", "");

            if (lobbyPlayer.getColor() == ColorPreference.getByName(colorId)) {
                lobbyPlayer.playSound("error");
                return;
            }

            lobbyPlayer.setSetting(Setting.COLOR_PREFERENCE, colorId);
            lobbyPlayer.playSound("pling");
            if (event.getInventory().getHolder() instanceof ColorPreferenceMenu menu) {
                menu.update();
            }
            return;
        }

        switch (itemId) {
            case "spawn_button":
                spawnButton(player);
                break;
            case "challenge_server":
                openSubMenu(lobbyPlayer, InventoryType.CHALLENGE, InventoryType.NAVIGATOR);
                break;
            case "archiv_server":
                openSubMenu(lobbyPlayer, InventoryType.ARCHIV, InventoryType.NAVIGATOR);
                break;
            case "secret_menu":
                openSubMenu(lobbyPlayer, InventoryType.SECRETS, InventoryType.PROFILE);
                break;
            case "statistic_menu":
                openSubMenu(lobbyPlayer, InventoryType.SPIELERINFO, InventoryType.PROFILE);
                break;
            case "settings_menu":
                openSubMenu(lobbyPlayer, InventoryType.SETTINGS, InventoryType.PROFILE);
                break;
            case "settings.color_preference":
                openSubMenu(lobbyPlayer, InventoryType.COLOR_PREFERENCE, InventoryType.SETTINGS);
                break;
            /*case "settings.global_playtime":
                openSubMenu(lobbyPlayer, InventoryType.PLAYTIME, InventoryType.SPIELERINFO);
                break;*/
            case "back_button":
                backButton(player, event.getInventory().getHolder());
                break;
            case "among_server":
                connectButton(lobbyPlayer,"among-us");
                break;
            case "duels_server":
                connectButton(lobbyPlayer,"duels");
                break;
            case "challenge-1_server":
                connectButton(lobbyPlayer,"challenge-1");
                break;
            case "challenge-2_server":
                connectButton(lobbyPlayer,"challenge-2");
                break;
            case "challenge-3_server":
                connectButton(lobbyPlayer,"challenge-3");
            case "archiv-1_server":
                connectButton(lobbyPlayer,"archiv-1");
                break;
            case "archiv-2_server":
                connectButton(lobbyPlayer,"archiv-2");
                break;
            case "event_server":
                //connectButton(player,"event_server");
                player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getLang().format("event-server-no-event", null)));
                lobbyPlayer.playSound("error");
                return;
            default:
                return;
        }

        lobbyPlayer.playSound("pling");
    }

    private void spawnButton(Player player) {
        player.performCommand("spawn");
        player.closeInventory();
    }

    private void backButton(Player player, InventoryHolder holder) {
        LobbyPlayer lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);
        if (lobbyPlayer == null) return;

        if (holder instanceof LobbyInventory menu) {
            InventoryType parent = menu.getParentType();

            if (parent == null) {
                player.closeInventory();
                return;
            }

            InventoryType grandParent = null;

            if (parent == InventoryType.SETTINGS || parent == InventoryType.SECRETS || parent == InventoryType.SPIELERINFO) {
                grandParent = InventoryType.PROFILE;
            }
            lobbyPlayer.openInventory(parent, grandParent);
        } else {
            player.closeInventory();
        }
    }

    private void connectButton(LobbyPlayer player, String serverName) {
        player.playSound("pling");
        player.sendToServer(serverName);
        //plugin.getLogger().info("Sende " + player + " zu " + serverName);
    }

    public void openSubMenu(LobbyPlayer player, InventoryType inventoryType, InventoryType parentType) {
        player.openInventory(inventoryType, parentType);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (inventory.getHolder() instanceof NavigatorMenu menu) {
            menu.stopUpdateTask();
        } else if (inventory.getHolder() instanceof ChallengeMenu menu) {
            menu.stopUpdateTask();
        } else if (inventory.getHolder() instanceof ArchivMenu menu) {
            menu.stopUpdateTask();
        }else if (inventory.getHolder() instanceof SpielerinfoMenu menu) {
            menu.stopUpdateTask();
        }
    }
}
