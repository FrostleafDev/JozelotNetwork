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
    private boolean showPlayerNameTags;
    private boolean entitiesFocusPlayer;
    private boolean entityGrief;
    private boolean canMobSpawn;

    private boolean dropItemsOnDeath;
    private boolean keepInventory;
    private boolean fireSpread;
    private boolean leafDecay;
    private boolean naturalRegeneration;
    private boolean daylightCycle;
    private boolean weatherCycle;
    private boolean announceAdvancements;
    private boolean advancementsEnabled;

    private String joinMessageType;
    private String leaveMessageType;

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

    public boolean isShowPlayerNameTags() {
        return showPlayerNameTags;
    }

    public String getJoinMessageType() {
        return joinMessageType;
    }

    public String getLeaveMessageType() {
        return leaveMessageType;
    }

    public boolean isEntitiesFocusPlayer() {
        return entitiesFocusPlayer;
    }

    public boolean isEntityGrief() {
        return entityGrief;
    }

    public boolean isCanMobSpawn() {
        return canMobSpawn;
    }

    public boolean isDropItemsOnDeath() {
        return dropItemsOnDeath;
    }

    public boolean isKeepInventory() {
        return keepInventory;
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public boolean isLeafDecay() {
        return leafDecay;
    }

    public boolean isNaturalRegeneration() {
        return naturalRegeneration;
    }

    public boolean isDaylightCycle() {
        return daylightCycle;
    }

    public boolean isWeatherCycle() {
        return weatherCycle;
    }

    public boolean isAnnounceAdvancements() {
        return announceAdvancements;
    }

    public boolean isAdvancementsEnabled() {
        return advancementsEnabled;
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

        showPlayerNameTags = plugin.getConfig().getBoolean("enable-player-name-tag");

        joinMessageType = plugin.getConfig().getString("join-messages");
        leaveMessageType = plugin.getConfig().getString("leave-messages");

        entitiesFocusPlayer = plugin.getConfig().getBoolean("entities-focus-player");

        entityGrief = plugin.getConfig().getBoolean("entity-grief");

        canMobSpawn = plugin.getConfig().getBoolean("can-mob-spawn");

        dropItemsOnDeath = plugin.getConfig().getBoolean("drop-items-on-death", false);
        keepInventory = plugin.getConfig().getBoolean("keep-inventory", true);
        fireSpread = plugin.getConfig().getBoolean("fire-spread", false);
        leafDecay = plugin.getConfig().getBoolean("leaf-decay", false);
        naturalRegeneration = plugin.getConfig().getBoolean("natural-regeneration", false);
        daylightCycle = plugin.getConfig().getBoolean("daylight-cycle", false);
        weatherCycle = plugin.getConfig().getBoolean("weather-cycle", false);
        announceAdvancements = plugin.getConfig().getBoolean("announce-advancements", false);
        advancementsEnabled = plugin.getConfig().getBoolean("advancements-enabled", false);
    }

}
