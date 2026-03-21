package de.jozelot.jozelotArchive.inventory;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.navigator.NavigatorMenu;
import org.bukkit.entity.Player;

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
}
