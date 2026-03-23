package de.jozelot.jozelotArchive.inventory.hotbar;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.player.user.User;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public abstract class HotbarItem {
    
    protected ItemStack item;
    protected int slot;
    protected final JozelotArchive plugin;
    protected final MiniMessage mm = MiniMessage.miniMessage();

    public static final NamespacedKey HOTBAR_KEY = new NamespacedKey("jozelotarchive", "hotbar_id");

    public HotbarItem(int slot, JozelotArchive plugin) {
        this.slot = slot;
        this.plugin = plugin;
    }

    public abstract void onInteract(User user, PlayerInteractEvent event);

    public abstract ItemStack getItem(User user);

    public int getSlot() {
        return slot;
    }
}
