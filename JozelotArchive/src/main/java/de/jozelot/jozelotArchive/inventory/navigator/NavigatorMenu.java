package de.jozelot.jozelotArchive.inventory.navigator;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.Menu;
import de.jozelot.jozelotArchive.player.user.User;

public class NavigatorMenu extends Menu {

    public NavigatorMenu(JozelotArchive plugin) {
        super(plugin, plugin.getServiceManager().getConfigManager().getInt("inventories.navigator.size"), plugin.getServiceManager().getConfigManager().getString("inventories.navigator.title"));
    }

    @Override
    public void setupItems(User user, Menu previousInventory) {
        int size = getInventory().getSize();

        setFiller(user, size);
        setBackButton(size - 9, user, previousInventory);
    }
}
