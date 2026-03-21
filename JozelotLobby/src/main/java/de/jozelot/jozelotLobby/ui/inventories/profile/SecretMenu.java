package de.jozelot.jozelotLobby.ui.inventories.profile;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.player.settings.ColorPreference;
import de.jozelot.jozelotLobby.secrets.objects.Secret;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SecretMenu extends LobbyInventory {

    private final Inventory inventory;
    private final JozelotLobby plugin;
    private final LobbyPlayer lobbyPlayer;
    private BukkitTask bukkitTask;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SecretMenu(JozelotLobby plugin, Player player) {
        this.plugin = plugin;
        this.lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        String title = plugin.getConfig().getString("inventories.secrets.title", "Secret Übersicht");

        int size = plugin.getSecretMgr().getSecrets().size();
        int rows = (int) Math.ceil(size / 9.0) + 2;
        rows = Math.min(rows, 6);
        if (rows == 2) rows = 3;
        this.inventory = Bukkit.createInventory(this, 9 * rows, mm.deserialize(title));

        fillBackGround();
        update();
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

        ItemStack info = new ItemStack(Material.PLAYER_HEAD);
        info.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", lobbyPlayer.getColor().getInfoIcon()));
            meta.setPlayerProfile(profile);
            meta.displayName(mm.deserialize(plugin.getConfig().getString("items.secret_info.name", "<white>Information")));

            List<String> lore = plugin.getConfig().getStringList("items.secret_info.lore");
            List<Component> fullLore = new ArrayList<>();
            for (String lorePart : lore) {
                fullLore.add(mm.deserialize(lorePart));
            }

            meta.lore(fullLore);
            meta.getPersistentDataContainer().set(HotbarItems.ITEM_ID, PersistentDataType.STRING, "info_button");
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
        });

        inventory.setItem(size - 9, backArrow);
        inventory.setItem(size - 1, info);
    }

    @Override
    public void update() {

        int slot = 9;
        for (Secret secret : plugin.getSecretMgr().getSecrets()) {
            setSecretItem(secret, slot);
            slot++;
        }
    }

    private void setSecretItem(Secret secret, int slot) {
        boolean found = lobbyPlayer.hasFoundSecret(secret.getId());
        String name = found ? "<#f7bc74>" + secret.getName() : "<gray>???";

        Material material = secret.getIcon();

        ItemStack item = new ItemStack(material);

        item.editMeta(meta -> {
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
            meta.displayName(mm.deserialize("<!italic>" + name));

            List<Component> lore = new ArrayList<>();

            if (found) {
                lore.addAll(wrapText(secret.getDescription(), 32));
            }

            lore.add(Component.empty());
            String status = found ? "<#00FC00>Gefunden" : "<#f90036>Nicht gefunden";
            lore.add(mm.deserialize("<!italic><dark_gray>» <gray>Status: " + status));

            meta.lore(lore);

            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
        });

        inventory.setItem(slot, item);
    }

    private List<net.kyori.adventure.text.Component> wrapText(String text, int maxLength) {
        List<net.kyori.adventure.text.Component> lines = new ArrayList<>();

        lines.add(net.kyori.adventure.text.Component.empty());

        StringBuilder currentLine = new StringBuilder();
        String baseTag = "<gray>";

        String[] words = text.split(" ");
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLength) {
                lines.add(mm.deserialize("<!italic>" + baseTag + currentLine.toString().trim()));
                currentLine.setLength(0);
            }
            currentLine.append(word).append(" ");
        }

        if (currentLine.length() > 0) {
            lines.add(mm.deserialize("<!italic>" + baseTag + currentLine.toString().trim()));
        }

        return lines;
    }


    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}