package de.jozelot.jozelotLobby.ui.inventories;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.ui.inventories.navigation.ArchivMenu;
import de.jozelot.jozelotLobby.ui.inventories.navigation.ChallengeMenu;
import de.jozelot.jozelotLobby.ui.inventories.navigation.NavigatorMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.util.function.BiFunction;

public enum InventoryType {
    NAVIGATOR(NavigatorMenu::new),
    PROFILE(NavigatorMenu::new),
    CHALLENGE(ChallengeMenu::new),
    ARCHIV(ArchivMenu::new);

    private final BiFunction<JozelotLobby, Player, InventoryHolder> factory;

    InventoryType(BiFunction<JozelotLobby, Player, InventoryHolder> factory) {
        this.factory = factory;
    }

    public InventoryHolder create(JozelotLobby plugin, Player player) {
        return factory.apply(plugin, player);
    }
}
