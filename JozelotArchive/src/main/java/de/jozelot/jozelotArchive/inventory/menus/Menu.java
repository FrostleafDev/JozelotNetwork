package de.jozelot.jozelotArchive.inventory.menus;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.player.user.Sound;
import de.jozelot.jozelotArchive.player.user.User;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public abstract class Menu implements InventoryHolder {

    protected JozelotArchive plugin;
    protected Inventory inventory;
    private final Map<Integer, BiConsumer<User, InventoryClickEvent>> actions = new HashMap<>();
    public static final NamespacedKey MENU_ITEM_KEY = new NamespacedKey("jozelotarchive", "menu_item_key");
    protected MiniMessage mm = MiniMessage.miniMessage();

    public Menu(JozelotArchive plugin, int size, String title) {
        this.plugin = plugin;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    public void open(User user) {
        open(user, null);
    }

    public void open(User user, InventoryType previousInventory) {
        setupItems(user, previousInventory);
        user.getPlayer().openInventory(getInventory());
    }

    public abstract void setupItems(User user, InventoryType previousInventory);

    public void setItem(int slot, ItemStack item, BiConsumer<User, InventoryClickEvent> action) {
        inventory.setItem(slot, item);
        if (action != null) {
            actions.put(slot, action);
        }
    }

    public void handleClick(int slot, User user, InventoryClickEvent event) {
        if (actions.containsKey(slot)) {
            actions.get(slot).accept(user, event);
        }
    }

    public void setBackButton(int slot, User rawUser, InventoryType previousMenu) {
        ItemStack arrow = new ItemStack(Material.PLAYER_HEAD);

        arrow.editMeta(SkullMeta.class, meta -> {
            meta.displayName(mm.deserialize(plugin.getServiceManager().getConfigManager().getString("items.back_arrow.name")));
            meta.lore(Collections.emptyList());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

            String b64 = rawUser.getColor().getBackArrow();

            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", rawUser.getColor().getBackArrow()));

            meta.setPlayerProfile(profile);
        });

        setItem(slot, arrow, (user, event) -> {
            if (previousMenu != null) {
                user.openInventory(previousMenu);
            } else {
                user.getPlayer().closeInventory();
            }
            user.playSound(Sound.PLING);
        });
    }

    public void setFiller(User user, int size) {
        Material rawMaterial = user.getColor().getFillerMaterial();

        if (rawMaterial == null) {
            rawMaterial = Material.BARRIER;
        }
        ItemStack filler = new ItemStack(rawMaterial);

        filler.editMeta(meta -> {
           meta.displayName(mm.deserialize(""));
           meta.setHideTooltip(true);
        });

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, filler);
        }
        for (int i = size - 9; i < size; i++) {
            inventory.setItem(i, filler);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
