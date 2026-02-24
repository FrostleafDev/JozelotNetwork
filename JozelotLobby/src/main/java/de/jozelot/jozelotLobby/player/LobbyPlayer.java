package de.jozelot.jozelotLobby.player;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.settings.ColorPreference;
import de.jozelot.jozelotLobby.player.settings.Setting;
import de.jozelot.jozelotLobby.ui.inventories.InventoryType;
import de.jozelot.jozelotLobby.ui.inventories.navigation.NavigatorMenu;
import de.jozelot.jozelotLobby.ui.items.HiderState;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LobbyPlayer {

    private final UUID uuid;
    private final JozelotLobby plugin;
    private HiderState hiderState;
    private Map<Setting, String> settings = new HashMap<>();


    public LobbyPlayer(UUID uuid, HiderState hiderState, JozelotLobby plugin) {
        this.hiderState = hiderState;
        this.uuid = uuid;
        this.plugin = plugin;
    }

    public void setHiderState(HiderState hiderState) {
        this.hiderState = hiderState;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getLobbyPlayerDatabase().setHiderState(this, hiderState);
        });
    }

    public HiderState getHiderState() {
        return hiderState;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void toggleHider() {
        setHiderState(getHiderState().next());
        updateVisibility();
    }

    public void updateVisibility() {
        Player player = getPlayer();
        if (player == null) return;

        Bukkit.getOnlinePlayers().stream()
                .filter(target -> !target.equals(player))
                .forEach(target -> {
                    switch (hiderState) {
                        case VISIBLE -> player.showPlayer(plugin, target);
                        case HIDDEN -> player.hidePlayer(plugin, target);
                        case TEAM -> {
                            if (target.hasPermission("network.lobby.player_hider.team")) {
                                player.showPlayer(plugin, target);
                            } else {
                                player.hidePlayer(plugin, target);
                            }
                        }
                    }
                });
    }

    /**
     * Spielt einen UI Sound aus der Lang der Proxy ab
     * @param soundKey
     */
    public void playSound(String soundKey) {
        Player player = getPlayer();

        String path = plugin.getLang().getRaw("sounds." + soundKey);

        if (path != null && !path.isEmpty()) {
            try {
                String cleanedPath = path.trim().toLowerCase();

                if (!cleanedPath.contains(":")) {
                    cleanedPath = "minecraft:" + cleanedPath;
                }

                Sound sound = Sound.sound(
                        Key.key(cleanedPath),
                        Sound.Source.UI,
                        1.0f,
                        1.0f
                );
                player.playSound(sound, Sound.Emitter.self());
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§cUngültiger Sound-Key in lang.yml: " + path);
            }
        }
    }

    /////////// SETTINGS ///////////
    public String getRawSetting(Setting setting) {
        return settings.getOrDefault(setting, setting.getDefaultValue());
    }

    public boolean getBoolean(Setting setting) {
        return Boolean.parseBoolean(getRawSetting(setting));
    }

    public int getInt(Setting setting) {
        return Integer.parseInt(getRawSetting(setting));
    }

    public ColorPreference getColor() {
        String colorValue = getRawSetting(Setting.COLOR_PREFERENCE);
        ColorPreference pref = ColorPreference.getByName(colorValue);
        return (pref != null) ? pref : ColorPreference.WHITE;
    }

    public void setSetting(Setting setting, String value) {
        this.settings.put(setting, value);
    }

    public void openInventory(InventoryType inventory) {
        Player player = getPlayer();
        switch (inventory) {
            case PROFILE:
                break;
            case NAVIGATOR:
                player.openInventory(new NavigatorMenu(plugin, player).getInventory());
                break;
        }
    }
}
