package de.jozelot.jozelotLobby.ui.inventories.profile;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.ui.inventories.InventoryType;
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

public class ProfileMenu implements InventoryHolder {

    private final Inventory inventory;
    private final JozelotLobby plugin;
    private final LobbyPlayer lobbyPlayer;
    private BukkitTask bukkitTask;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ProfileMenu(JozelotLobby plugin, Player player) {
        this.plugin = plugin;
        this.lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        String title = plugin.getConfig().getString("inventories.profile.title", "Profil");
        this.inventory = Bukkit.createInventory(this, 9 * 3, mm.deserialize(title));

        fillBackGround();
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

        for (int i : new int[]{0,1,2,3,4,5,6,7,8,19,20,21,22,23,24,25,26}) {
            inventory.setItem(i, filler);
        }

        // Back Arrow
        ItemStack backArrow = new ItemStack(Material.PLAYER_HEAD);
        backArrow.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", lobbyPlayer.getColor().getBackArrow()));
            meta.setPlayerProfile(profile);
            meta.displayName(mm.deserialize(plugin.getConfig().getString("items.back_arrow.name", "<red>Zurück")));
            meta.getPersistentDataContainer().set(HotbarItems.ITEM_ID, PersistentDataType.STRING, "back_button");
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
        });
        inventory.setItem(18, backArrow);
    }

    public void update() {
        // SECRETS
        setupServerItem(11, "secret_menu");

        // STATISTICS
        setupServerItem(13, "statistic_menu");

        // SETTINGS
        setupServerItem(15, "settings_menu");
    }

    private void setupServerItem(int slot, String configKey) {
        String path = "items." + configKey;

        String matName = plugin.getConfig().getString(path + ".item", "BARRIER");
        Material mat = Material.matchMaterial(matName != null ? matName : "BARRIER");
        ItemStack item = new ItemStack(mat != null ? mat : Material.BARRIER);

        item.editMeta(meta -> {

            String name = plugin.getConfig().getString(path + ".name");
            meta.displayName(mm.deserialize(name != null ? name : "<red>FEHLER: Name fehlt"));

            List<String> configLore = plugin.getConfig().getStringList(path + ".lore");
            List<Component> loreComponents = new ArrayList<>();
            for (String line : configLore) {
                if (line == null) continue;
                String replaced = line.replace("{secrets_current}", "0")
                        .replace("{secrets_max}", "0");
                loreComponents.add(mm.deserialize(replaced));
            }
            meta.lore(loreComponents);

            if (meta instanceof SkullMeta skullMeta) {
                String b64 = plugin.getConfig().getString(path + ".base64");
                if (b64 != null && !b64.isEmpty()) {
                    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                    profile.setProperty(new ProfileProperty("textures", b64));
                    skullMeta.setPlayerProfile(profile);
                }
            }

            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(HotbarItems.ITEM_ID, PersistentDataType.STRING, configKey);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });

        inventory.setItem(slot, item);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}