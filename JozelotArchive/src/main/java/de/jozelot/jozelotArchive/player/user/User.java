package de.jozelot.jozelotArchive.player.user;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.hotbar.HotbarItemType;
import de.jozelot.jozelotArchive.inventory.hotbar.items.HiderState;
import de.jozelot.jozelotArchive.inventory.menus.InventoryType;
import de.jozelot.jozelotArchive.inventory.menus.Menu;
import de.jozelot.jozelotArchive.inventory.menus.MenuManager;
import de.jozelot.jozelotArchive.inventory.menus.navigator.locations.LocationSort;
import de.jozelot.jozelotArchive.inventory.menus.navigator.player.PlayerSort;
import de.jozelot.jozelotArchive.player.user.settings.ColorPreference;
import de.jozelot.jozelotArchive.player.user.settings.Setting;
import de.jozelot.jozelotArchive.utility.LuckPermsUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import javax.swing.plaf.PanelUI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class User
{
    private final UUID uuid;
    private final JozelotArchive plugin;
    private long lastGameModeSwap = 0;
    private HiderState hiderState;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Setting, String> settings = new HashMap<>();

    public User(UUID uuid, JozelotArchive plugin) {
        this.uuid = uuid;
        this.plugin = plugin;
        this.hiderState = plugin.getServiceManager().getUserDatabase().getHiderState(uuid);
    }

    public void giveHotbarItems() {
        var hm = plugin.getServiceManager().getHotbarManager();

        hm.giveItem(this, HotbarItemType.NAVIGATOR);
        hm.giveItem(this, HotbarItemType.GAMEMODE_CHANGER);
        hm.giveItem(this, HotbarItemType.PLAYER_HIDER);
    }

    public void clearInventory() {
        getPlayer().getInventory().clear();
    }

    public void updateItem(HotbarItemType type) {
        var hm = plugin.getServiceManager().getHotbarManager();
        hm.giveItem(this, type);
    }

    public void setHiderState(HiderState hiderState) {
        this.hiderState = hiderState;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getServiceManager().getUserDatabase().setHiderState(this, hiderState);
        });
    }

    public HiderState getHiderState() {
        return hiderState;
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

    public void openInventory(InventoryType type) {
        openInventory(type, null, null);
    }

    public void openInventory(InventoryType type, InventoryType previousType) {
        openInventory(type, previousType, null);
    }

    public void openInventory(InventoryType type, InventoryType previousType, Object data) {
        MenuManager mm = plugin.getServiceManager().getMenuManager();

        Menu menu = mm.createMenu(type, getPlayer(), data);

        if (menu != null) {
            menu.open(this, previousType);
        }
    }

    /**
     * Cycles the players gamemode from survival to spectator
     * @return the gamemode after the change
     */
    @Deprecated(
            forRemoval = true
    )
    public GameMode cycleGameMode() {
        Player player = getPlayer();
        GameMode nextGameMode = player.getGameMode() == GameMode.SPECTATOR ? GameMode.SURVIVAL : GameMode.SPECTATOR;

        player.setGameMode(nextGameMode);
        player.setAllowFlight(true);
        player.setFlying(true);
        playSound(de.jozelot.jozelotArchive.player.user.Sound.PLING);
        lastGameModeSwap = System.currentTimeMillis();

        sendGameModeActionBar();
        return nextGameMode;
    }

    @Deprecated(
            forRemoval = true
    )
    public boolean canGameModeSwap() {
        return lastGameModeSwap < System.currentTimeMillis() - 1000;
    }

    @Deprecated(
            forRemoval = true
    )
    public void sendGameModeActionBar() {
        getPlayer().sendActionBar(mm.deserialize(plugin.getServiceManager().getLangManager().format("archive-gamemode-hotkey-info",
                Map.of("gamemode", getPlayer().getGameMode() == GameMode.SPECTATOR ? "Survival" : "Specator")
        )));
    }

    /**
     * Spielt einen UI Sound aus der Lang der Proxy ab
     * @param soundKey
     */
    public void playSound(String soundKey) {
        Player player = getPlayer();

        String path = plugin.getServiceManager().getLangManager().getRaw("sounds." + soundKey);

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

    public void playSound(de.jozelot.jozelotArchive.player.user.Sound sound) {
        String soundKey = sound.name().toLowerCase();
        playSound(soundKey);
    }

    public ColorPreference getColor() {
        String colorValue = getRawSetting(Setting.COLOR_PREFERENCE);
        ColorPreference pref = ColorPreference.getByName(colorValue);
        return (pref != null) ? pref : ColorPreference.WHITE;
    }

    public LocationSort getLocationSort() {
        String sortValue = getRawSetting(Setting.LOCATION_SORT);
        LocationSort pref = LocationSort.getByName(sortValue);
        return (pref != null) ? pref : LocationSort.NAME;
    }

    public PlayerSort getPlayerSort() {
        String sortValue = getRawSetting(Setting.PLAYER_SORT);
        PlayerSort pref = PlayerSort.getByName(sortValue);
        return (pref != null) ? pref : PlayerSort.NAME;
    }

    public void toggleLocationSort() {
        LocationSort current = getLocationSort();
        LocationSort next = current.next();

        setSetting(Setting.LOCATION_SORT, next.name());

        saveSettingAsync(Setting.LOCATION_SORT, next.name());
    }

    public void togglePlayerSort() {
        PlayerSort current = getPlayerSort();
        PlayerSort next = current.next();

        setSetting(Setting.PLAYER_SORT, next.name());

        saveSettingAsync(Setting.PLAYER_SORT, next.name());
    }

    private void saveSettingAsync(Setting setting, String value) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getServiceManager().getUserDatabase().updateSetting(uuid, setting, value);
        });
    }



    public String getRawSetting(Setting setting) {
        return settings.getOrDefault(setting, setting.getDefaultValue());
    }

    public boolean getBoolean(Setting setting) {
        return Boolean.parseBoolean(getRawSetting(setting));
    }

    public int getInt(Setting setting) {
        return Integer.parseInt(getRawSetting(setting));
    }

    public void setSetting(Setting setting, String value) {
        this.settings.put(setting, value);
    }

    public void setSettings(Map<Setting, String> settingStringMap) {
        settings.putAll(settingStringMap);
    }

    public Map<Setting, String> getSettings() {
        return settings;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public String getRank() {
        return LuckPermsUtils.getPlayerRankAsString(getPlayer());
    }
}
