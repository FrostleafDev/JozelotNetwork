package de.jozelot.jozelotLobby.ui.inventories;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.ui.inventories.navigation.ArchivMenu;
import de.jozelot.jozelotLobby.ui.inventories.navigation.ChallengeMenu;
import de.jozelot.jozelotLobby.ui.inventories.navigation.NavigatorMenu;
import de.jozelot.jozelotLobby.ui.inventories.profile.PlaytimeMenu;
import de.jozelot.jozelotLobby.ui.inventories.profile.ProfileMenu;
import de.jozelot.jozelotLobby.ui.inventories.profile.SecretMenu;
import de.jozelot.jozelotLobby.ui.inventories.profile.SpielerinfoMenu;
import de.jozelot.jozelotLobby.ui.inventories.profile.settings.ColorPreferenceMenu;
import de.jozelot.jozelotLobby.ui.inventories.profile.settings.SettingsMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.util.function.BiFunction;

public enum InventoryType {
    NAVIGATOR(NavigatorMenu::new),
    PROFILE(ProfileMenu::new),
    CHALLENGE(ChallengeMenu::new),
    ARCHIV(ArchivMenu::new),
    SECRETS(SecretMenu::new),
    SPIELERINFO(SpielerinfoMenu::new),
    SETTINGS(SettingsMenu::new),
    COLOR_PREFERENCE(ColorPreferenceMenu::new),
    PLAYTIME(PlaytimeMenu::new);

    private final BiFunction<JozelotLobby, Player, InventoryHolder> factory;

    InventoryType(BiFunction<JozelotLobby, Player, InventoryHolder> factory) {
        this.factory = factory;
    }

    public InventoryHolder create(JozelotLobby plugin, Player player) {
        return factory.apply(plugin, player);
    }
}
