package de.jozelot.jozelotLobby.ui.inventories.profile;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.ui.inventories.InventoryType;
import de.jozelot.jozelotLobby.ui.inventories.LobbyInventory;
import de.jozelot.jozelotLobby.ui.items.HotbarItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SpielerinfoMenu extends LobbyInventory {

    private final Inventory inventory;
    private final JozelotLobby plugin;
    private final LobbyPlayer lobbyPlayer;
    private BukkitTask bukkitTask;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SpielerinfoMenu(JozelotLobby plugin, Player player) {
        this.plugin = plugin;
        this.lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        String title = plugin.getConfig().getString("inventories.playerinfo.title" + player.getName(), "Spielerinfo - " + player.getName());
        this.inventory = Bukkit.createInventory(this, 9 * 3, mm.deserialize(title));

        fillBackGround();
        update();
        startUpdateTask();
    }

    public void fillBackGround() {
        ItemStack filler = new ItemStack(lobbyPlayer.getColor().getFillerMaterial());
        filler.editMeta(meta -> {
            meta.displayName(Component.empty());
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
            meta.setHideTooltip(true);
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
            meta.lore(Collections.emptyList());
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        });
        inventory.setItem(size - 9, backArrow);
        inventory.setItem(13, getPlayerInfoItem());
    }

    @Override
    public void update() {
        inventory.setItem(11, getGlobalTimeItem());
        inventory.setItem(15, getRankItem());
    }



    private ItemStack getRankItem() {
        ItemStack item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("items.settings.rank.item")));
        Player player = lobbyPlayer.getPlayer();

        item.editMeta(meta -> {
            meta.displayName(mm.deserialize(plugin.getConfig().getString("items.settings.rank.name")));

            List<String> configLore = plugin.getConfig().getStringList("items.settings.rank.lore");
            List<Component> loreComponents = new ArrayList<>();
            for (String line : configLore) {
                if (line == null) continue;
                String replaced = line
                        .replace("{rank}", lobbyPlayer.getRank());
                loreComponents.add(mm.deserialize(replaced));
            }
            meta.lore(loreComponents);

            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(HotbarItems.ITEM_ID, PersistentDataType.STRING, "settings.rank");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });

        return item;
    }

    private ItemStack getPlayerInfoItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        Player player = lobbyPlayer.getPlayer();

        item.editMeta(meta -> {
            if (meta instanceof SkullMeta skullMeta) {
                skullMeta.setOwningPlayer(player);
            }
            meta.displayName(mm.deserialize("<!italic><green>" + player.getName()));

            List<String> itemDescription = List.of(
                    "",
                    "<!italic><dark_gray>» <gray>Status: <#00FC00>Online",
                    "<!italic><dark_gray>» <gray>Server: <aqua>Lobby");

            List<Component> lore = itemDescription.stream()
                    .map(mm::deserialize)
                    .toList();

            meta.lore(lore);
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });

        return item;
    }

    private ItemStack getGlobalTimeItem() {
        ItemStack item = new ItemStack(Material.getMaterial(plugin.getConfig().getString("items.settings.global_playtime.item")));
        Player player = lobbyPlayer.getPlayer();

        item.editMeta(meta -> {
            meta.displayName(mm.deserialize(plugin.getConfig().getString("items.settings.global_playtime.name")));

            List<String> configLore = plugin.getConfig().getStringList("items.settings.global_playtime.lore");
            List<Component> loreComponents = new ArrayList<>();
            for (String line : configLore) {
                if (line == null) continue;
                String replaced = line
                        .replace("{playtime}", lobbyPlayer.getFormattedPlaytime2());
                loreComponents.add(mm.deserialize(replaced));
            }
            meta.lore(loreComponents);

            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(HotbarItems.ITEM_ID, PersistentDataType.STRING, "settings.global_playtime");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });

        return item;
    }

    public void startUpdateTask() {
        this.bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this::update, 0L, 60L);
    }

    public void stopUpdateTask() {
        if (this.bukkitTask != null) this.bukkitTask.cancel();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}