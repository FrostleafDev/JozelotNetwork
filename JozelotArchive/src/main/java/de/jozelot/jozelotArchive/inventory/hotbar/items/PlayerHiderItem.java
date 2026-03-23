package de.jozelot.jozelotArchive.inventory.hotbar.items;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.hotbar.HotbarItem;
import de.jozelot.jozelotArchive.inventory.hotbar.HotbarItemType;
import de.jozelot.jozelotArchive.player.user.User;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerHiderItem extends HotbarItem {

    public PlayerHiderItem(int slot, JozelotArchive plugin) {
        super(slot, plugin);
    }

    private ItemStack createItem(User user) {
        var cm = plugin.getServiceManager().getConfigManager();

        HiderState state = user.getHiderState();

        Material material;
        String name;

        switch (state) {
            case VISIBLE -> {
                material = Material.getMaterial(cm.getString("items.player_hider.visible_item"));
                name = cm.getString("items.player_hider.visible_name");
                break;
            }
            case TEAM -> {
                material = Material.getMaterial(cm.getString("items.player_hider.team_item"));
                name = cm.getString("items.player_hider.team_name");
                break;
            }
            case HIDDEN -> {
                material = Material.getMaterial(cm.getString("items.player_hider.hidden_item"));
                name = cm.getString("items.player_hider.hidden_name");
                break;
            }
            case null, default -> {
                material =  Material.BARRIER;
                name = "NaN";
                break;
            }
        }

        ItemStack item = new ItemStack(material);

        item.editMeta(meta -> {
            meta.displayName(mm.deserialize(name));
            meta.getPersistentDataContainer().set(HOTBAR_KEY, PersistentDataType.STRING,"player_hider");

            meta.lore(cm.getStringList("items.player_hider.description").stream().map(mm::deserialize).toList());
            meta.addItemFlags(ItemFlag.values());

            if (meta instanceof LeatherArmorMeta leatherMeta) {
                switch (state) {
                    case VISIBLE:
                        leatherMeta.setColor(Color.LIME);
                        break;
                    case TEAM:
                        leatherMeta.setColor(Color.PURPLE);
                        break;
                    case HIDDEN:
                        leatherMeta.setColor(Color.RED);
                        break;
                    case null, default:
                        leatherMeta.setColor(Color.GRAY);
                        break;
                }
            }

        });
        return item;
    }

    @Override
    public void onInteract(User user, PlayerInteractEvent event) {
        event.setCancelled(true);
        user.playSound("pling");

        user.getPlayer().setCooldown(event.getMaterial(), 20);
        user.toggleHider();
        user.updateItem(HotbarItemType.PLAYER_HIDER);
    }

    @Override
    public ItemStack getItem(User user) {
        return createItem(user);
    }
}
