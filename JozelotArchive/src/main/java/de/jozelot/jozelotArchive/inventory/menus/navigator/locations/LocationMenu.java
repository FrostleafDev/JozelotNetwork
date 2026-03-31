package de.jozelot.jozelotArchive.inventory.menus.navigator.locations;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.hotbar.HotbarItem;
import de.jozelot.jozelotArchive.inventory.menus.InventoryType;
import de.jozelot.jozelotArchive.inventory.menus.Menu;
import de.jozelot.jozelotArchive.location.Location;
import de.jozelot.jozelotArchive.player.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class LocationMenu extends Menu {

    public LocationMenu(JozelotArchive plugin, Location location) {
        super(plugin, plugin.getServiceManager().getConfigManager().getInt("inventories.location_menu.size"), plugin.getServiceManager().getConfigManager().getString("inventories.location_menu.title"));
    }

    @Override
    public void setupItems(User user, InventoryType previousInventory) {
        var cm = plugin.getServiceManager().getConfigManager();
        int size = getInventory().getSize();

        setFiller(user, size);
        setBackButton(size - 9, user, previousInventory);
    }

}
