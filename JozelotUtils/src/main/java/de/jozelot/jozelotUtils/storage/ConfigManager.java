package de.jozelot.jozelotUtils.storage;

import de.jozelot.jozelotUtils.JozelotUtils;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final JozelotUtils plugin;

    private String redisHost;
    private String redisPassword;
    private int redisPort;

    private String colorPrimary;
    private String colorSecondary;
    private String colorTertiary;
    private String colorDanger;
    private String colorGrey;
    private String defaultGamemode;
    private boolean isAutomaticFlight;
    private boolean isAutomaticFlightPlayer;
    private boolean canBuild;
    private boolean inventoryLocked;
    private boolean canTakeDamage;
    private boolean canHunger;

    public ConfigManager(JozelotUtils plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public String getRedisHost() {
        return redisHost;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getColorGrey() {
        return colorGrey;
    }

    public String getColorDanger() {
        return colorDanger;
    }

    public String getColorTertiary() {
        return colorTertiary;
    }

    public String getColorSecondary() {
        return colorSecondary;
    }

    public String getColorPrimary() {
        return colorPrimary;
    }

    public boolean isAutomaticFlight() {
        return isAutomaticFlight;
    }

    public boolean isAutomaticFlightPlayer() {
        return isAutomaticFlightPlayer;
    }

    public String getDefaultGamemode() {
        return defaultGamemode;
    }

    public boolean canBuild() {
        return canBuild;
    }

    public boolean isInventoryLocked() {
        return inventoryLocked;
    }

    public boolean canTakeDamage() {
        return canTakeDamage;
    }

    public boolean canHunger() {
        return canHunger;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();

        redisHost = plugin.getConfig().getString("redis.host");
        redisPassword = plugin.getConfig().getString("redis.password");
        redisPort = plugin.getConfig().getInt("redis.port");

        colorPrimary = plugin.getConfig().getString("color-settings.primary");
        colorSecondary = plugin.getConfig().getString("color-settings.secondary");
        colorTertiary = plugin.getConfig().getString("color-settings.tertiary");
        colorDanger = plugin.getConfig().getString("color-settings.danger");
        colorGrey = plugin.getConfig().getString("color-settings.grey");

        isAutomaticFlight = plugin.getConfig().getBoolean("automatic-flight-for-admins");
        isAutomaticFlightPlayer = plugin.getConfig().getBoolean("automatic-flight-for-player");
        defaultGamemode = plugin.getConfig().getString("default-gamemode");

        canBuild = plugin.getConfig().getBoolean("can-build");
        inventoryLocked = plugin.getConfig().getBoolean("inventory-locked");
        canTakeDamage = plugin.getConfig().getBoolean("can-take-damage");
        canHunger = plugin.getConfig().getBoolean("can-hunger");
    }

}
