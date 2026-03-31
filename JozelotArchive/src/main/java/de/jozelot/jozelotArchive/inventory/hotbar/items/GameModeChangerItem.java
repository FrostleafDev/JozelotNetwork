package de.jozelot.jozelotArchive.inventory.hotbar.items;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.hotbar.HotbarItem;
import de.jozelot.jozelotArchive.inventory.hotbar.HotbarItemType;
import de.jozelot.jozelotArchive.player.user.Sound;
import de.jozelot.jozelotArchive.player.user.User;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class GameModeChangerItem extends HotbarItem {

    public GameModeChangerItem(int slot, JozelotArchive plugin) {
        super(slot, plugin);
    }

    private ItemStack createItem(User user) {
        var cm = plugin.getServiceManager().getConfigManager();
        Material material = Material.getMaterial(cm.getString("items.gamemode_changer.item"));

        if (material == null) {
            material = Material.BARRIER;
        }

        ItemStack item = new ItemStack(material);

        item.editMeta(meta -> {
            if (user.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                meta.displayName(mm.deserialize(cm.getString("items.gamemode_changer.name_sp")));
            } else {
                meta.displayName(mm.deserialize(cm.getString("items.gamemode_changer.name_s")));
            }
            meta.getPersistentDataContainer().set(HOTBAR_KEY, PersistentDataType.STRING,"gamemode_changer");

            meta.lore(cm.getStringList("items.gamemode_changer.description").stream().map(mm::deserialize).toList());
        });
        return item;
    }

    @Override
    public void onInteract(User user, PlayerInteractEvent event) {
        GameMode gameMode = user.getPlayer().getGameMode();
        event.setCancelled(true);
        if (gameMode == GameMode.SPECTATOR) {
            user.getPlayer().setGameMode(GameMode.SURVIVAL);
        } else {
            user.getPlayer().setGameMode(GameMode.SPECTATOR);
        }

        Material material = event.getItem().getType();
        user.getPlayer().setCooldown(material, 20);
        user.playSound(Sound.PLING);
        user.updateItem(HotbarItemType.GAMEMODE_CHANGER);
    }

    @Override
    public ItemStack getItem(User user) {
        return createItem(user);
    }
}
