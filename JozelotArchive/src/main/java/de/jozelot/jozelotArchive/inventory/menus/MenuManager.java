package de.jozelot.jozelotArchive.inventory.menus;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.menus.navigator.NavigatorMenu;
import de.jozelot.jozelotArchive.inventory.menus.navigator.locations.LocationMenu;
import de.jozelot.jozelotArchive.inventory.menus.navigator.locations.LocationOverviewMenu;
import de.jozelot.jozelotArchive.inventory.menus.navigator.player.PlayerOverviewMenu;
import de.jozelot.jozelotArchive.inventory.menus.navigator.project.ProjectMenu;
import de.jozelot.jozelotArchive.location.Location;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class MenuManager {

    private JozelotArchive plugin;

    // Key: InventoryType (Type für das Inventar), Player: Der, der es öffnet, Object: Übergabe Objekt fürs Menu, Menu: Die InventoryHolder Klasse
    private Map<InventoryType, BiFunction<Player, Object, Menu>> menuFactory  = new HashMap<>();

    public MenuManager(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public void registerMenus() {
        menuFactory.clear();
        menuFactory.put(InventoryType.NAVIGATOR, (player, data) -> new NavigatorMenu(plugin));
        menuFactory.put(InventoryType.LOCATION_OVERVIEW, (player, data) -> {
            Collection<Location> locations = (Collection<Location>) data;
            return new LocationOverviewMenu(plugin, locations);
        });
        menuFactory.put(InventoryType.LOCATION_INFO, (player, data) -> {
            Location location = (Location) data;
            return new LocationMenu(plugin, location);
        });
        menuFactory.put(InventoryType.PLAYER_OVERVIEW, (player, data) -> new PlayerOverviewMenu(plugin));
        menuFactory.put(InventoryType.PROJECT_INFO, (player, data) -> new ProjectMenu(plugin));

        /*menuFactory.put(InventoryType.SERVER_INFO, (player, data) -> new NavigatorMenu(plugin));

        menuFactory.put(InventoryType.PLAYER_INFO, (player, data) -> {
            Player target = (Player) data; // Wir casten das Objekt zum Spieler
            return new PlayerInfoMenu(plugin, target);
        });*/
    }

    public Menu createMenu(InventoryType type, Player player, Object data) {
        if (!menuFactory.containsKey(type)) return null;

        BiFunction<Player, Object, Menu> factory = menuFactory.get(type);
        if (factory == null) return null;
        return factory.apply(player, data);
    }

    public void handleReload() {
        registerMenus();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.closeInventory();
        }
    }
}
