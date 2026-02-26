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
import org.bukkit.NamespacedKey;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class NavigatorMenu implements InventoryHolder {

    private final Inventory inventory;
    private final JozelotLobby plugin;
    private final LobbyPlayer lobbyPlayer;
    private final Player player;
    private BukkitTask bukkitTask;

    private final MiniMessage mm = MiniMessage.miniMessage();

    public NavigatorMenu(JozelotLobby plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 9 * 5, mm.deserialize(plugin.getConfig().getString("inventories.navigator.title")));

        this.lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        fillBackGround();
        update();
        startUpdateTask();
    }

    public void fillBackGround() {
        ItemStack filler = new ItemStack(lobbyPlayer.getColor().getFillerMaterial());
        filler.editMeta(meta -> {
            meta.displayName(mm.deserialize(" "));
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
        });

        ItemStack backArrow = new ItemStack(Material.PLAYER_HEAD);

        backArrow.editMeta(SkullMeta.class, skullMeta -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
            profile.setProperty(new ProfileProperty("textures", lobbyPlayer.getColor().getBackArrow()));
            skullMeta.setPlayerProfile(profile);

            String name = plugin.getConfig().getString("items.back_arrow.name");
            skullMeta.displayName(mm.deserialize(name != null ? name : "<red>Zurück"));

            skullMeta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
            skullMeta.getPersistentDataContainer().set(HotbarItems.ITEM_ID, PersistentDataType.STRING, "back_button");
        });

        Material spawnMaterial = Material.getMaterial(plugin.getConfig().getString("items.spawn_button.item"));

        if (spawnMaterial == null) {
            spawnMaterial = Material.BARRIER;
        }

        ItemStack spawnButton = new ItemStack(spawnMaterial);

        spawnButton.editMeta(meta -> {
            meta.displayName(mm.deserialize(plugin.getConfig().getString("items.spawn_button.name")));
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(HotbarItems.ITEM_ID, PersistentDataType.STRING, "spawn_button");
        });

        for (int i : new int[]{0,1,2,3,4,5,6,7,8,37,38,39,41,42,43,44}) {
            inventory.setItem(i, filler);
        }

        inventory.setItem(36, backArrow);
        inventory.setItem(40, spawnButton);
    }

    public void update() {
        Material challengeServerMaterial = Material.getMaterial(plugin.getConfig().getString("items.challenge_server.item"));

        if (challengeServerMaterial == null) {
            challengeServerMaterial = Material.BARRIER;
        }

        ItemStack challengeServer = new ItemStack(challengeServerMaterial);

        int challengeServerPlayerCount = 0;

        challengeServerPlayerCount += plugin.getNetworkStateManager().getServer("challenge-1").players();
        challengeServerPlayerCount += plugin.getNetworkStateManager().getServer("challenge-2").players();
        challengeServerPlayerCount += plugin.getNetworkStateManager().getServer("challenge-3").players();

        String finalChallengeServerPlayerCount = challengeServerPlayerCount < 0 ? "<#00FC00>" + challengeServerPlayerCount : "<#f90036>" + challengeServerPlayerCount;

        int challengeServerCount = 0;
        int maxChallengeServerCount = 3;

        challengeServerCount += plugin.getNetworkStateManager().getServer("challenge-1").online() ? 1 : 0;
        challengeServerCount += plugin.getNetworkStateManager().getServer("challenge-2").online() ? 1 : 0;
        challengeServerCount += plugin.getNetworkStateManager().getServer("challenge-3").online() ? 1 : 0;

        String finalChallegeServerCount = challengeServerCount < maxChallengeServerCount ? "<#00FC00>" + challengeServerCount : "<#f90036>" + challengeServerCount;

        challengeServer.editMeta(meta -> {
            meta.displayName(mm.deserialize(plugin.getConfig().getString("items.challenge_server.name")));
            meta.getPersistentDataContainer().set(HotbarItems.IS_PROTECTED, PersistentDataType.BOOLEAN, true);
            meta.getPersistentDataContainer().set(HotbarItems.ITEM_ID, PersistentDataType.STRING, "challenge_server");

            List<String> itemDescription = plugin.getConfig().getStringList("items.challenge_server.lore");

            List<Component> lore = itemDescription.stream()
                    .map(line -> line.replace("{online_players}", finalChallengeServerPlayerCount))
                    .map(line -> line.replace("{online_servers}", finalChallegeServerCount))
                    .map(line -> line.replace("{max_servers}", String.valueOf(maxChallengeServerCount)))
                    .map(mm::deserialize)
                    .toList();

            meta.lore(lore);
            meta.addItemFlags(ItemFlag.values());
        });

        inventory.setItem(13, challengeServer);

    }

    public void startUpdateTask() {
        this.bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this::update, 0L, 60L);
    }

    public void stopUpdateTask() {
        if (this.bukkitTask != null) {
            this.bukkitTask.cancel();
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
