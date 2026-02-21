package de.jozelot.jozelotLobby.items;

import de.jozelot.jozelotLobby.JozelotLobby;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class HotbarItems {

    private JozelotLobby plugin;
    private MiniMessage mm = MiniMessage.miniMessage();

    public HotbarItems(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    public ItemStack getNavigator() {
        String itemName = plugin.getConfig().getString("items.navigator.name");
        String item = plugin.getConfig().getString("items.navigator.item");
        List<String> itemDescription = plugin.getConfig().getStringList("items.navigator.description");

        if (itemName == null || item == null|| itemDescription.isEmpty()) {
            plugin.getLogger().info("Fehler in Navigator Konfiguration");
            return null;
        }

        Material material = Material.getMaterial(item);

        if (material == null) {
            material = Material.BARRIER;
        }

        ItemStack itemStack = new ItemStack(material);

        ItemMeta navigatorMeta = itemStack.getItemMeta();

        navigatorMeta.itemName(mm.deserialize(itemName));

        List<Component> lore = itemDescription.stream()
                .map(mm::deserialize)
                .toList();

        navigatorMeta.lore(lore);

        itemStack.setItemMeta(navigatorMeta);

        return itemStack;
    }
}
