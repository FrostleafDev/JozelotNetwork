package de.jozelot.jozelotArchive.inventory.menus.navigator;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.hotbar.HotbarItem;
import de.jozelot.jozelotArchive.inventory.menus.InventoryType;
import de.jozelot.jozelotArchive.inventory.menus.Menu;
import de.jozelot.jozelotArchive.player.user.Sound;
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
    public void setupItems(User user, InventoryType previousInventory) {
        var cm = plugin.getServiceManager().getConfigManager();
        int size = getInventory().getSize();
        WorldType type = WorldType.fromWorld(user.getPlayer().getWorld());

        setFiller(user, size);
        setBackButton(size - 9, user, previousInventory);
        setSpawnButton(size - 5);
        setWorldChangeButton(9, WorldType.OVERWORLD, type);
        setWorldChangeButton(18, WorldType.NETHER, type);
        setWorldChangeButton(27, WorldType.END, type);

        setLocationOverview(cm.getInt("items.location_overview.slot"));
        setPlayerOverview(cm.getInt("items.player_overview.slot"));
        setProjectOverview(cm.getInt("items.project_overview.slot"));
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
            user.playSound(Sound.SUCCESS);
        }));
    }

    private void setWorldChangeButton(int slot, WorldType type, WorldType currentType) {
        var cm = plugin.getServiceManager().getConfigManager();
        boolean isInWorld = type == currentType;

        Material material;
        String name;
        String current = " <dark_gray>[Aktuell]";

        switch (type) {
            case OVERWORLD -> {
                material = Material.getMaterial(cm.getString("items.world_change.overworld_item"));
                name = isInWorld ? cm.getString("items.world_change.overworld_name") + current : cm.getString("items.world_change.overworld_name");
            }
            case NETHER -> {
                material = Material.getMaterial(cm.getString("items.world_change.nether_item"));
                name = isInWorld ? cm.getString("items.world_change.nether_name") + current : cm.getString("items.world_change.nether_name");
            }
            case END -> {
                material = Material.getMaterial(cm.getString("items.world_change.end_item"));
                name = isInWorld ? cm.getString("items.world_change.endd_name") + current : cm.getString("items.world_change.end_name");
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
                user.playSound(Sound.ERROR);
                return;
            }

            World targetWorld = getTargetWorld(type);
            if (targetWorld == null) return;

            Location targetLoc = calculateTargetLocation(player.getLocation(), targetWorld);

            player.teleport(targetLoc);
            player.closeInventory();
            user.playSound(Sound.SUCCESS);
        }));
    }

    private void setLocationOverview(int slot) {
        var cm = plugin.getServiceManager().getConfigManager();
        Material material = Material.getMaterial(plugin.getServiceManager().getConfigManager().getString("items.location_overview.item"));

        if (material == null) {
            material = Material.BARRIER;
        }

        ItemStack item = new ItemStack(material);

        int location_count = plugin.getServiceManager().getLocationManager().getLocationCount();

        item.editMeta(meta -> {
            meta.displayName(mm.deserialize(plugin.getServiceManager().getConfigManager().getString("items.location_overview.name")));
            meta.lore(cm.getStringList("items.location_overview.description").stream()
                    .map(line -> line.replace("{location_count}", location_count > 0 ? "<white>" + location_count : "<#f90036>" + location_count))
                    .map(mm::deserialize)
                    .toList());
        });

        setItem(slot, item, ((user, event) -> {
            if (plugin.getServiceManager().getLocationManager().getLocationCount() == 0) {
                user.getPlayer().sendMessage(mm.deserialize(plugin.getServiceManager().getLangManager().format("archive-no-locations", null)));
                user.playSound(Sound.ERROR);
                return;
            }

            user.playSound(Sound.PLING);
            user.openInventory(InventoryType.LOCATION_OVERVIEW, InventoryType.NAVIGATOR, plugin.getServiceManager().getLocationManager().getLocationsAsCollection());
        }));
    }

    private void setPlayerOverview(int slot) {
        var cm = plugin.getServiceManager().getConfigManager();
        Material material = Material.getMaterial(plugin.getServiceManager().getConfigManager().getString("items.player_overview.item"));

        if (material == null) {
            material = Material.BARRIER;
        }

        ItemStack item = new ItemStack(material);

        // TODO: Implement player objects
        int player_count = 46;

        item.editMeta(meta -> {
            meta.displayName(mm.deserialize(plugin.getServiceManager().getConfigManager().getString("items.player_overview.name")));
            meta.lore(cm.getStringList("items.player_overview.description").stream()
                    .map(line -> line.replace("{player_count}", player_count > 0 ? "<white>" + player_count : "<#f90036>" + player_count))
                    .map(mm::deserialize)
                    .toList());
        });

        setItem(slot, item, ((user, event) -> {
            user.playSound(Sound.PLING);
            user.openInventory(InventoryType.PLAYER_OVERVIEW, InventoryType.NAVIGATOR);
        }));
    }

    private void setProjectOverview(int slot) {
        var cm = plugin.getServiceManager().getConfigManager();
        Material material = Material.getMaterial(plugin.getServiceManager().getConfigManager().getString("items.project_overview.item"));

        if (material == null) {
            material = Material.BARRIER;
        }

        ItemStack item = new ItemStack(material);


        item.editMeta(meta -> {
            meta.displayName(mm.deserialize(plugin.getServiceManager().getConfigManager().getString("items.project_overview.name")));
            meta.lore(cm.getStringList("items.project_overview.description").stream()
                    .map(mm::deserialize)
                    .toList());
        });

        setItem(slot, item, ((user, event) -> {
            user.playSound(Sound.PLING);
            user.openInventory(InventoryType.PROJECT_INFO, InventoryType.NAVIGATOR);
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

    public static WorldType fromWorld(World world) {
        return switch (world.getEnvironment()) {
            case NORMAL -> OVERWORLD;
            case NETHER -> NETHER;
            case THE_END -> END;
            default -> null;
        };
    }
}
