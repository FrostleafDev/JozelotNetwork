package de.jozelot.jozelotArchive.inventory;

import de.jozelot.jozelotArchive.JozelotArchive;
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
}
