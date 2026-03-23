package de.jozelot.jozelotArchive.inventory.hotbar;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.hotbar.items.GameModeChangerItem;
import de.jozelot.jozelotArchive.inventory.hotbar.items.NavigatorItem;
import de.jozelot.jozelotArchive.inventory.hotbar.items.PlayerHiderItem;
import de.jozelot.jozelotArchive.player.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class HotbarManager {

    private final JozelotArchive plugin;
    private final Map<HotbarItemType, HotbarItem> items = new HashMap<>();

    public HotbarManager(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public void registerItems() {
        var cm = plugin.getServiceManager().getConfigManager();
        items.clear();
        items.put(HotbarItemType.NAVIGATOR, new NavigatorItem(cm.getInt("items.navigator.slot"), plugin));
        items.put(HotbarItemType.GAMEMODE_CHANGER, new GameModeChangerItem(cm.getInt("items.gamemode_changer.slot"), plugin));
        items.put(HotbarItemType.PLAYER_HIDER, new PlayerHiderItem(cm.getInt("items.player_hider.slot"), plugin));
    }

    public void giveItem(User user, HotbarItemType type) {
        HotbarItem hotbarItem = items.get(type);
        if (hotbarItem == null) return;

        user.getPlayer().getInventory().setItem(hotbarItem.getSlot(), hotbarItem.getItem(user));
    }

    public HotbarItem getItemById(String id) {
         id = id.toUpperCase();
         try {
             return items.get(HotbarItemType.valueOf(id));
         } catch (IllegalArgumentException e) {
             return null;
         }
    }

    public void clearHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, null);
        }
    }

    public void handleReload() {
        registerItems();
        Bukkit.getOnlinePlayers().forEach(player -> {
            clearHotbar(player);

            plugin.getServiceManager().getUserManager().getUsersAsCollection().forEach(user -> {
                user.updateVisibility();
                user.giveHotbarItems();
            });
        });
    }
}
