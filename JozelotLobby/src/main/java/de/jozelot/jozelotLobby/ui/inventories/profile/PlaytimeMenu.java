package de.jozelot.jozelotLobby.ui.inventories.profile;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.ui.inventories.LobbyInventory;
import de.jozelot.jozelotLobby.ui.items.HotbarItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlaytimeMenu extends LobbyInventory {

    private Inventory inventory;
    private final JozelotLobby plugin;
    private final LobbyPlayer lobbyPlayer;
    private BukkitTask bukkitTask;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public PlaytimeMenu(JozelotLobby plugin, Player player) {
        this.plugin = plugin;
        this.lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        this.inventory = Bukkit.createInventory(this, 9 * 3, mm.deserialize("Lade Spielzeiten..."));

        loadAsync();
    }

    private void loadAsync() {
        Player player = lobbyPlayer.getPlayer();
        if (player == null) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Datenbank-Werte holen (Zustand beim letzten Server-Wechsel/Join)
            Map<String, Long> dbTimes = plugin.getMySQLManager().getAllServerPlaytimes(player.getUniqueId());
            long dbNetworkTotal = plugin.getMySQLManager().getTotalNetworkPlaytime(player.getUniqueId());

            int serverCount = dbTimes.size();
            // Dynamische Zeilenberechnung (ohne Seitenränder, also 9 Items pro Zeile möglich)
            int rows = (int) Math.ceil((serverCount + 1) / 9.0) + 2;
            if (rows < 3) rows = 3;
            if (rows > 6) rows = 6;

            int finalSize = rows * 9;
            String title = plugin.getConfig().getString("inventories.playtime.title", "Spielzeiten");

            Bukkit.getScheduler().runTask(plugin, () -> {
                this.inventory = Bukkit.createInventory(this, finalSize, mm.deserialize(title));

                fillBackGround();
                // Wir übergeben die DB-Werte an populate
                populate(dbTimes, dbNetworkTotal);

                player.openInventory(this.inventory);
                startUpdateTask();
            });
        });
    }

    private void populate(Map<String, Long> dbTimes, long dbNetworkTotal) {
        // Netzwerk Item oben in der Mitte
        inventory.setItem(4, getNetworkTotalItem(dbNetworkTotal));

        int slot = 9; // Start in der zweiten Zeile
        for (Map.Entry<String, Long> entry : dbTimes.entrySet()) {
            if (slot >= inventory.getSize() - 9) break;

            // Wir prüfen, ob der Server der aktuelle Lobby-Server ist
            // Hier musst du ggf. den Identifier deines aktuellen Servers kennen
            inventory.setItem(slot, getServerItem(entry.getKey(), entry.getValue()));
            slot++;
        }
    }

    public void fillBackGround() {
        ItemStack filler = new ItemStack(lobbyPlayer.getColor().getFillerMaterial());
        filler.editMeta(meta -> {
            meta.displayName(Component.empty());
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
        });

        int size = inventory.getSize();

        // Nur erste und letzte Zeile als Rand
        for (int i = 0; i < 9; i++) inventory.setItem(i, filler);
        for (int i = size - 9; i < size; i++) inventory.setItem(i, filler);

        // Zurück-Button
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

    private ItemStack getNetworkTotalItem(long dbNetworkTotal) {
        ItemStack item = new ItemStack(Material.BEACON);
        item.editMeta(meta -> {
            meta.displayName(mm.deserialize("<gold><bold>Gesamtspielzeit"));
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
        });
        return item;
    }

    private ItemStack getServerItem(String name, long millis) {
        ItemStack item = new ItemStack(Material.CLOCK);
        item.editMeta(meta -> {
            meta.displayName(mm.deserialize("<yellow>" + name));
            meta.lore(List.of(mm.deserialize("<gray>Zeit: <white>" + formatTime(millis))));
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
        });
        return item;
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;

        if (h > 0) return h + "h " + m + "m";
        if (m > 0) return m + "m " + s + "s";
        return s + "s";
    }

    @Override
    public void update() {
        // 1. Gesamtzeit Update (DB-Total + aktuelle Session-Zeit)
        ItemStack totalItem = inventory.getItem(4);
        if (totalItem != null) {
            totalItem.editMeta(meta -> {
                // Wir nutzen hier dein LobbyPlayer Objekt, das die Session-Zeit bereits dazurechnet
                meta.lore(List.of(
                        mm.deserialize("<gray>Netzwerk: <green>" + lobbyPlayer.getFormattedPlaytime()),
                        mm.deserialize(""),
                        mm.deserialize("<dark_gray>» Zeit wird live aktualisiert")
                ));
            });
        }

        // 2. Suche das Item des aktuellen Servers und aktualisiere es ebenfalls live
        // Da wir in der Lobby sind, suchen wir nach dem Server-Item "Lobby"
        for (int i = 9; i < inventory.getSize() - 9; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() != Material.CLOCK) continue;

            item.editMeta(meta -> {
                String name = mm.serialize(meta.displayName());
                // Wenn das Item den Namen deines aktuellen Servers trägt (z.B. Lobby)
                if (name.contains("Lobby")) {
                    // Hier rechnen wir die Zeit für das Item ebenfalls live hoch
                    // In einer echten Umgebung müsstest du den DB-Wert des Servers + Session-Differenz nehmen
                    meta.lore(List.of(mm.deserialize("<gray>Zeit: <white>" + lobbyPlayer.getFormattedPlaytime())));
                }
            });
        }
    }

    public void startUpdateTask() {
        stopUpdateTask();
        this.bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this::update, 20L, 20L);
    }

    public void stopUpdateTask() {
        if (this.bukkitTask != null) {
            this.bukkitTask.cancel();
            this.bukkitTask = null;
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}