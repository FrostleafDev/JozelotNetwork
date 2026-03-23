package de.jozelot.jozelotArchive.inventory.hotbar.items;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.hotbar.HotbarItem;
import de.jozelot.jozelotArchive.inventory.menus.InventoryType;
import de.jozelot.jozelotArchive.player.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class NavigatorItem extends HotbarItem {

    public NavigatorItem(int slot, JozelotArchive plugin) {
        super(slot, plugin);
        this.item = createItem();
    }

    private ItemStack createItem() {
        var cm = plugin.getServiceManager().getConfigManager();
        Material material = Material.getMaterial(cm.getString("items.navigator.item"));

        if (material == null) {
            material = Material.BARRIER;
        }

        ItemStack item = new ItemStack(material);

        item.editMeta(meta -> {
            meta.displayName(mm.deserialize(cm.getString("items.navigator.name")));
            meta.getPersistentDataContainer().set(HOTBAR_KEY, PersistentDataType.STRING,"navigator");

            meta.lore(cm.getStringList("items.navigator.description").stream().map(mm::deserialize).toList());
        });
        return item;
    }

    @Override
    public void onInteract(User user, PlayerInteractEvent event) {
        user.openInventory(InventoryType.NAVIGATOR);
        user.playSound("pling");
        event.setCancelled(true);
    }

    @Override
    public ItemStack getItem(User user) {
        return this.item;
    }
}
