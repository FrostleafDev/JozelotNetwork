package de.jozelot.jozelotArchive.inventory.menus.navigator.project;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.menus.InventoryType;
import de.jozelot.jozelotArchive.inventory.menus.Menu;
import de.jozelot.jozelotArchive.player.user.User;

public class ProjectMenu extends Menu {

    public ProjectMenu(JozelotArchive plugin) {
        super(plugin, plugin.getServiceManager().getConfigManager().getInt("inventories.navigator.size"), plugin.getServiceManager().getConfigManager().getString("inventories.navigator.title"));
    }

    @Override
    public void setupItems(User user, InventoryType previousInventory) {
        var cm = plugin.getServiceManager().getConfigManager();
        int size = getInventory().getSize();

        setFiller(user, size);
        setBackButton(size - 9, user, previousInventory);
    }
}
