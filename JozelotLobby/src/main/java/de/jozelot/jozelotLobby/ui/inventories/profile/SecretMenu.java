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

public class SecretMenu implements InventoryHolder{

    private final Inventory inventory;
    private final JozelotLobby plugin;
    private final LobbyPlayer lobbyPlayer;
    private BukkitTask bukkitTask;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SecretMenu(JozelotLobby plugin, Player player) {
        this.plugin = plugin;
        this.lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        String title = plugin.getConfig().getString("inventories.secrets.title", "Secret Übersicht");
        this.inventory = Bukkit.createInventory(this, 9 * 4, mm.deserialize(title));

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

        for (int i : new int[]{0,1,2,3,4,5,6,7,8,27,28,29,30,31,32,33,34,35}) {
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
        inventory.setItem(27, backArrow);
    }

    public void update() {
    }


    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}