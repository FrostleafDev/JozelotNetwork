package de.jozelot.jozelotLobby.ui.inventories.profile.settings;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.player.settings.ColorPreference;
import de.jozelot.jozelotLobby.ui.inventories.InventoryType;
import de.jozelot.jozelotLobby.ui.inventories.LobbyInventory;
import de.jozelot.jozelotLobby.ui.items.HotbarItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ColorPreferenceMenu extends LobbyInventory {

    private final Inventory inventory;
    private final JozelotLobby plugin;
    private final LobbyPlayer lobbyPlayer;
    private BukkitTask bukkitTask;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ColorPreferenceMenu(JozelotLobby plugin, Player player) {
        this.plugin = plugin;
        this.lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        String title = plugin.getConfig().getString("inventories.color_preference.title", "Menufarben");
        int colors = ColorPreference.values().length;

        int rows = (int) Math.ceil(colors / 9.0) + 2;
        rows = Math.min(rows, 6);
        this.inventory = Bukkit.createInventory(this, 9 * rows, mm.deserialize(title));

        update();
    }

    private InventoryType parentType = null;

    public void setParentType(InventoryType parentType) {
        this.parentType = parentType;
    }

    public InventoryType getParentType() {
        return parentType;
    }

    public void fillBackGround() {
        ItemStack filler = new ItemStack(lobbyPlayer.getColor().getFillerMaterial());
        filler.editMeta(meta -> {
            meta.displayName(Component.empty());
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
        });

        int size = inventory.getSize();

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, filler);
        }

        for (int i = size - 9; i < size; i++) {
            inventory.setItem(i, filler);
        }

        ItemStack backArrow = new ItemStack(Material.PLAYER_HEAD);
        backArrow.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", lobbyPlayer.getColor().getBackArrow()));
            meta.setPlayerProfile(profile);
            meta.displayName(mm.deserialize(plugin.getConfig().getString("items.back_arrow.name", "<red>Zurück")));
            meta.getPersistentDataContainer().set(HotbarItems.ITEM_ID, PersistentDataType.STRING, "back_button");
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
        });
        inventory.setItem(size - 9, backArrow);
    }

    @Override
    public void update() {
        fillBackGround();
        for (int i = 0; i < ColorPreference.values().length; i++) {
            setColor(i);
        }
    }

    private void setColor(int i) {
        ColorPreference[] colors = ColorPreference.values();
        ColorPreference color = colors[i];

        Material material = color.getIcon();

        ItemStack item = new ItemStack(material);

        item.editMeta(meta -> {
            meta.getPersistentDataContainer().set(HotbarItems.ITEM_ID, PersistentDataType.STRING, "settings.color." + color.name());
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
            String info = color == lobbyPlayer.getColor() ? "<dark_gray> [Aktuell]" : "";
            meta.displayName(mm.deserialize("<!italic><white>" + color.getName() + info));
        });

        inventory.setItem(i + 9, item);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}