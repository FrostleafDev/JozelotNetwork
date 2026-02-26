package de.jozelot.jozelotLobby.ui.inventories.navigation;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.ui.items.HotbarItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NavigatorMenu implements InventoryHolder {

    private final Inventory inventory;
    private final JozelotLobby plugin;
    private final LobbyPlayer lobbyPlayer;
    private BukkitTask bukkitTask;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public NavigatorMenu(JozelotLobby plugin, Player player) {
        this.plugin = plugin;
        this.lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        String title = plugin.getConfig().getString("inventories.navigator.title", "Navigator");
        this.inventory = Bukkit.createInventory(this, 9 * 5, mm.deserialize(title));

        fillBackGround();
        update();
        startUpdateTask();
    }

    public void fillBackGround() {
        ItemStack filler = new ItemStack(lobbyPlayer.getColor().getFillerMaterial());
        filler.editMeta(meta -> {
            meta.displayName(Component.empty());
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
        });

        for (int i : new int[]{0,1,2,3,4,5,6,7,8,37,38,39,41,42,43,44}) {
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
        });
        inventory.setItem(36, backArrow);

        // Spawn Button
        Material spawnMat = Material.matchMaterial(plugin.getConfig().getString("items.spawn_button.item", "MAGMA_CREAM"));
        ItemStack spawnButton = new ItemStack(spawnMat != null ? spawnMat : Material.MAGMA_CREAM);
        spawnButton.editMeta(meta -> {
            meta.displayName(mm.deserialize(plugin.getConfig().getString("items.spawn_button.name", "<gold>Spawn")));
            meta.getPersistentDataContainer().set(HotbarItems.ITEM_ID, PersistentDataType.STRING, "spawn_button");
        });
        inventory.setItem(40, spawnButton);
    }

    public void update() {
        // Challenges
        setupServerItem(13, "challenge_server", new String[]{"challenge-1", "challenge-2", "challenge-3"}, true);

        // Archiv
        setupServerItem(21, "archiv_server", new String[]{"archiv-1", "archiv-2"}, true);

        // Among Us
        setupServerItem(19, "among_server", new String[]{"among-us"}, false);

        // Duels
        setupServerItem(23, "duels_server", new String[]{"duels"}, false);

        // Event
        String eventSrv = plugin.getConfig().getString("items.event_server.server", "");
        setupServerItem(25, "event_server", eventSrv.isEmpty() ? new String[0] : new String[]{eventSrv}, false);
    }

    private void setupServerItem(int slot, String configKey, String[] serverIds, boolean isMulti) {
        String path = "items." + configKey;

        // Material holen
        String matName = plugin.getConfig().getString(path + ".item", "BARRIER");
        Material mat = Material.matchMaterial(matName != null ? matName : "BARRIER");
        ItemStack item = new ItemStack(mat != null ? mat : Material.BARRIER);

        // Netzwerk Daten
        int totalPlayers = 0;
        int onlineCount = 0;
        for (String id : serverIds) {
            var state = plugin.getNetworkStateManager().getServer(id);
            if (state != null) {
                totalPlayers += state.players();
                if (state.online()) onlineCount++;
            }
        }

        // Platzhalter vorbereiten
        final String finalPlayers = (totalPlayers > 0 ? "<#00FC00>" : "<#f90036>") + totalPlayers;
        final String finalStatus;
        if (serverIds.length == 0) {
            finalStatus = "<#f90036>Kein Event";
        } else if (isMulti) {
            finalStatus = (onlineCount < serverIds.length ? "<#f90036>" : "<#00FC00>") + onlineCount;
        } else {
            finalStatus = onlineCount > 0 ? "<#00FC00>Online" : "<#f90036>Offline";
        }

        // Meta anwenden
        item.editMeta(meta -> {
            // Name
            String name = plugin.getConfig().getString(path + ".name");
            meta.displayName(mm.deserialize(name != null ? name : "<red>FEHLER: Name fehlt"));

            // Lore
            List<String> configLore = plugin.getConfig().getStringList(path + ".lore");
            List<Component> loreComponents = new ArrayList<>();
            for (String line : configLore) {
                if (line == null) continue;
                String replaced = line.replace("{online_players}", finalPlayers)
                        .replace("{online_servers}", finalStatus)
                        .replace("{status}", finalStatus)
                        .replace("{max_servers}", String.valueOf(serverIds.length));
                loreComponents.add(mm.deserialize(replaced));
            }
            meta.lore(loreComponents);

            // Kopf Textur
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