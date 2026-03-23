package de.jozelot.jozelotArchive.inventory.menus.navigator;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.hotbar.HotbarItem;
import de.jozelot.jozelotArchive.inventory.menus.Menu;
import de.jozelot.jozelotArchive.player.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class NavigatorMenu extends Menu {

    public NavigatorMenu(JozelotArchive plugin) {
        super(plugin, plugin.getServiceManager().getConfigManager().getInt("inventories.navigator.size"), plugin.getServiceManager().getConfigManager().getString("inventories.navigator.title"));
    }

    @Override
    public void setupItems(User user, Menu previousInventory) {
        int size = getInventory().getSize();

        setFiller(user, size);
        setBackButton(size - 9, user, previousInventory);
        setSpawnButton(size - 5);
        setWorldChangeButton(9, WorldType.OVERWORLD);
        setWorldChangeButton(18, WorldType.NETHER);
        setWorldChangeButton(27, WorldType.END);
    }

    private void setSpawnButton(int slot) {
        Material material = Material.getMaterial(plugin.getServiceManager().getConfigManager().getString("items.spawn_button.item"));

        if (material == null) {
            material = Material.BARRIER;
        }

        ItemStack item = new ItemStack(material);

        item.editMeta(meta -> {
            meta.displayName(mm.deserialize(plugin.getServiceManager().getConfigManager().getString("items.spawn_button.name")));
        });

        setItem(slot, item, ((user, event) -> {
            Player player = user.getPlayer();
            player.closeInventory();
            player.performCommand("spawn");
            user.playSound("success");
        }));
    }

    private void setWorldChangeButton(int slot, WorldType type) {
        var cm = plugin.getServiceManager().getConfigManager();

        Material material;
        String name;

        switch (type) {
            case OVERWORLD -> {
                material = Material.getMaterial(cm.getString("items.world_change.overworld_item"));
                name = cm.getString("items.world_change.overworld_name");
            }
            case NETHER -> {
                material = Material.getMaterial(cm.getString("items.world_change.nether_item"));
                name = cm.getString("items.world_change.nether_name");
            }
            case END -> {
                material = Material.getMaterial(cm.getString("items.world_change.end_item"));
                name = cm.getString("items.world_change.end_name");
            }
            case null, default -> {
                material = Material.BARRIER;
                name = "NaN";
            }
        }

        ItemStack item = new ItemStack(material);

        item.editMeta(meta -> {
            meta.displayName(mm.deserialize(name));
            meta.lore(cm.getStringList("items.world_change.lore").stream().map(mm::deserialize).toList());
            meta.getPersistentDataContainer().set(MENU_ITEM_KEY, PersistentDataType.STRING,"teleport_to_" + type.toString().toLowerCase());
        });

        setItem(slot, item, ((user, event) -> {
            Player player = user.getPlayer();
            World currentWorld = player.getWorld();
            World.Environment currentEnv = currentWorld.getEnvironment();

            if (isSameDimension(currentEnv, type)) {
                user.playSound("error");
                return;
            }

            World targetWorld = getTargetWorld(type);
            if (targetWorld == null) return;

            Location targetLoc = calculateTargetLocation(player.getLocation(), targetWorld);

            player.teleport(targetLoc);
            player.closeInventory();
            user.playSound("success");
        }));
    }

    private boolean isSameDimension(World.Environment env, WorldType type) {
        return (env == World.Environment.NORMAL && type == WorldType.OVERWORLD) ||
                (env == World.Environment.NETHER && type == WorldType.NETHER) ||
                (env == World.Environment.THE_END && type == WorldType.END);
    }

    private World getTargetWorld(WorldType type) {
        return switch (type) {
            case OVERWORLD -> Bukkit.getWorld("world");
            case NETHER -> Bukkit.getWorld("world_nether");
            case END -> Bukkit.getWorld("world_the_end");
        };
    }

    private Location calculateTargetLocation(Location from, World targetWorld) {
        double x = from.getX();
        double y = from.getY();
        double z = from.getZ();
        World.Environment fromEnv = from.getWorld().getEnvironment();
        World.Environment toEnv = targetWorld.getEnvironment();

        if (fromEnv == World.Environment.NORMAL && toEnv == World.Environment.NETHER) {
            x /= 8.0;
            z /= 8.0;
        } else if (fromEnv == World.Environment.NETHER && toEnv == World.Environment.NORMAL) {
            x *= 8.0;
            z *= 8.0;
        }

        if (toEnv == World.Environment.NETHER) {
            y = Math.min(y, 120);
        }

        return new Location(targetWorld, x, y, z, from.getYaw(), from.getPitch());
    }
}

enum WorldType {
    OVERWORLD,
    NETHER,
    END;
}
