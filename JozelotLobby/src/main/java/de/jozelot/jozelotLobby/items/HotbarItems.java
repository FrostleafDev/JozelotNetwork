package de.jozelot.jozelotLobby.items;

import com.sun.jna.platform.unix.X11;
import de.jozelot.jozelotLobby.JozelotLobby;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class HotbarItems {

    private final JozelotLobby plugin;
    private MiniMessage mm = MiniMessage.miniMessage();

    public static final NamespacedKey ITEM_ID = new NamespacedKey("jozelotlobby", "item_id");
    public static final NamespacedKey IS_PROTECTED = new NamespacedKey("jozelotlobby", "protected");

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

        navigatorMeta.displayName(mm.deserialize(itemName));
        navigatorMeta.addItemFlags(ItemFlag.values());

        List<Component> lore = itemDescription.stream()
                .map(mm::deserialize)
                .toList();

        navigatorMeta.lore(lore);
        navigatorMeta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "navigator");
        navigatorMeta.getPersistentDataContainer().set(IS_PROTECTED, PersistentDataType.BOOLEAN, true);

        itemStack.setItemMeta(navigatorMeta);

        return itemStack;
    }

    public ItemStack getPlayerHider(HiderState state) {
        String stateName = state.name().toLowerCase();
        String itemName = plugin.getConfig().getString("items.player_hider." + stateName + "_name");

        String item = plugin.getConfig().getString("items.player_hider." + stateName + "_item");
        List<String> itemDescription = plugin.getConfig().getStringList("items.player_hider.description");

        if (itemName == null || item == null|| itemDescription.isEmpty()) {
            plugin.getLogger().info("Fehler in Player Hider Konfiguration");
            return null;
        }

        Material material = Material.getMaterial(item);

        if (material == null) {
            material = Material.BARRIER;
        }

        ItemStack itemStack = new ItemStack(material);

        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.displayName(mm.deserialize(itemName));
        itemMeta.addItemFlags(ItemFlag.values());

        List<Component> lore = itemDescription.stream()
                .map(mm::deserialize)
                .toList();

        itemMeta.lore(lore);
        itemMeta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "player_hider");
        itemMeta.getPersistentDataContainer().set(IS_PROTECTED, PersistentDataType.BOOLEAN, true);

        if (itemMeta instanceof LeatherArmorMeta leatherMeta) {
            switch (state) {
                case VISIBLE:
                    leatherMeta.setColor(Color.LIME);
                    break;
                case TEAM:
                    leatherMeta.setColor(Color.PURPLE);
                    break;
                case HIDDEN:
                    leatherMeta.setColor(Color.RED);
                    break;
                default:
                    leatherMeta.setColor(Color.GRAY);
                    break;
            }
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack getProfile(Player player) {
        String itemName = plugin.getConfig().getString("items.profile.name");
        String item = plugin.getConfig().getString("items.profile.item");
        List<String> itemDescription = plugin.getConfig().getStringList("items.profile.description");

        if (itemName == null || item == null|| itemDescription.isEmpty()) {
            plugin.getLogger().info("Fehler in Profil Konfiguration");
            return null;
        }

        Material material = Material.getMaterial(item);

        if (material == null) {
            material = Material.BARRIER;
        }

        ItemStack itemStack = new ItemStack(material);

        ItemMeta itemMeta = itemStack.getItemMeta();


        itemMeta.addItemFlags(ItemFlag.values());

        List<Component> lore = itemDescription.stream()
                .map(mm::deserialize)
                .toList();

        itemMeta.lore(lore);
        itemMeta.displayName(mm.deserialize(itemName));
        itemMeta.getPersistentDataContainer().set(ITEM_ID, PersistentDataType.STRING, "profile");
        itemMeta.getPersistentDataContainer().set(IS_PROTECTED, PersistentDataType.BOOLEAN, true);

        if (itemMeta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}
