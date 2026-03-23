package de.jozelot.jozelotArchive.listener;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.hotbar.HotbarItem;
import de.jozelot.jozelotArchive.player.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class HotbarItemClickListener implements Listener {

    private JozelotArchive plugin;

    public HotbarItemClickListener(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;

        String id = item.getItemMeta().getPersistentDataContainer().get(HotbarItem.HOTBAR_KEY, PersistentDataType.STRING);
        if (id == null) return;


        var hm = plugin.getServiceManager().getHotbarManager();
        HotbarItem hotbarItem = hm.getItemById(id);

        if (hotbarItem != null) {
            Player player = event.getPlayer();
            if (player.hasCooldown(event.getMaterial())) return;

            User user = plugin.getServiceManager().getUserManager().getUser(player);
            if (user != null) {
                hotbarItem.onInteract(user, event);
            }
        }
    }
}
