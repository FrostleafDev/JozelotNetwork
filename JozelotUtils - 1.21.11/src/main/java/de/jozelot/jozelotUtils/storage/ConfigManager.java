package de.jozelot.jozelotUtils.storage;

import de.jozelot.jozelotUtils.JozelotUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final JozelotUtils plugin;

    private String redisHost;
    private String redisPassword;
    private int redisPort;

    private String mysqlHost;
    private String mysqlUser;
    private String mysqlPassword;
    private String mysqlDatabase;
    private int mysqlPort;

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
    private int tickSpeed;
    private boolean isCustomExperienceLevel;
    private int customExperienceLevel;
    private boolean isCustomBarLevel;
    private int customBarLevel;
    private boolean blockPortals;

    private boolean doInsomnia;
    private boolean spawnPhantoms;
    private boolean spawnWanderingTrader;
    private boolean spawnPatrols;
    private boolean blockDrops;
    private int playerSleepingPercentage;
    private boolean mobGriefing;

    private boolean isChatDisabled;
    private boolean isLocatorBar;
    private boolean ticksFreeze;

    private boolean spawnCommand;
    private boolean spawnOnFall;

    public int getFallOffHeight() {
        return fallOffHeight;
    }

    public boolean isSpawnOnFall() {
        return spawnOnFall;
    }

    private int fallOffHeight;

    private String joinMessageType;
    private String leaveMessageType;

    private Location spawnLocation;
    private boolean spawnOnJoin;

    private int defaultHotbarSlot;

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

    public int getTickSpeed() {
        return tickSpeed;
    }

    public boolean isTicksFreeze() {
        return ticksFreeze;
    }

    public boolean isCustomExperienceLevel() {
        return isCustomExperienceLevel;
    }

    public int getCustomExperienceLevel() {
        return customExperienceLevel;
    }

    public boolean isCustomBarLevel() {
        return isCustomBarLevel;
    }

    public int getCustomBarLevel() {
        return customBarLevel;
    }

    public boolean isChatDisabled() {
        return isChatDisabled;
    }

    public boolean isLocatorBar() {
        return isLocatorBar;
    }

    public boolean isBlockPortals() {
        return blockPortals;
    }

    public boolean isSpawnCommand() {
        return spawnCommand;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public boolean isSpawnOnJoin() {
        return spawnOnJoin;
    }

    public void setSpawnLocation(org.bukkit.Location loc) {
        this.spawnLocation = loc;

        List<Double> posList = new java.util.ArrayList<>();
        posList.add(loc.getX());
        posList.add(loc.getY());
        posList.add(loc.getZ());
        posList.add((double) loc.getYaw());
        posList.add((double) loc.getPitch());

        plugin.getConfig().set("spawn-position", posList);
        plugin.saveConfig();
    }

    public int getDefaultHotbarSlot() {
        return defaultHotbarSlot;
    }

    public boolean isSpawnPhantoms() { return spawnPhantoms; }
    public boolean isMobGriefing() { return mobGriefing; }
    public boolean isDoInsomnia() { return doInsomnia; }
    public boolean isBlockDrops() { return blockDrops; }
    public int getPlayerSleepingPercentage() { return playerSleepingPercentage; }
    public boolean isSpawnWanderingTrader() { return spawnWanderingTrader; }
    public boolean isSpawnPatrols() { return spawnPatrols; }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public String getMysqlUser() {
        return mysqlUser;
    }

    public String getMysqlHost() {
        return mysqlHost;
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
        ticksFreeze = plugin.getConfig().getBoolean("tick-freeze", false);
        tickSpeed = plugin.getConfig().getInt("tick-speed");

        isCustomExperienceLevel = plugin.getConfig().getBoolean("custom-experience-level");
        customExperienceLevel = plugin.getConfig().getInt("experience-level-set");

        isCustomBarLevel = plugin.getConfig().getBoolean("custom-bar-level");
        customBarLevel = plugin.getConfig().getInt("bar-level-set");

        isChatDisabled = plugin.getConfig().getBoolean("disable-default-chat");
        isLocatorBar = plugin.getConfig().getBoolean("locator-bar");

        blockPortals = plugin.getConfig().getBoolean("block-portals");

        spawnCommand = plugin.getConfig().getBoolean("spawn-command");

        spawnOnJoin = plugin.getConfig().getBoolean("spawn-on-join");

        List<Double> pos = plugin.getConfig().getDoubleList("spawn-position");

        if (pos.size() >= 3) {
            org.bukkit.World world = org.bukkit.Bukkit.getWorlds().get(0);

            double x = pos.get(0);
            double y = pos.get(1);
            double z = pos.get(2);
            float yaw = pos.size() >= 4 ? pos.get(3).floatValue() : 0.0f;
            float pitch = pos.size() >= 5 ? pos.get(4).floatValue() : 0.0f;

            spawnLocation = new org.bukkit.Location(world, x, y, z, yaw, pitch);
        }

        spawnOnJoin = plugin.getConfig().getBoolean("spawn-on-join");

        fallOffHeight = plugin.getConfig().getInt("fall-off-height");
        spawnOnFall = plugin.getConfig().getBoolean("spawn-on-fall-off");

        defaultHotbarSlot = plugin.getConfig().getInt("default-hotbar-slot");

        mobGriefing = plugin.getConfig().getBoolean("mob-griefing", false);
        doInsomnia = plugin.getConfig().getBoolean("do-insomnia", false);
        blockDrops = plugin.getConfig().getBoolean("block-drops", false);
        playerSleepingPercentage = plugin.getConfig().getInt("player-sleeping-percentage", 100);
        spawnPhantoms = plugin.getConfig().getBoolean("spawn-phantoms", false);
        spawnWanderingTrader = plugin.getConfig().getBoolean("spawn-wandering-trader", false); // Beachte den Tippfehler 'traider' aus deiner Vorlage
        spawnPatrols = plugin.getConfig().getBoolean("spawn-patrols", false);

        mysqlDatabase = plugin.getConfig().getString("mysql.database");
        mysqlPassword = plugin.getConfig().getString("mysql.password");
        mysqlUser = plugin.getConfig().getString("mysql.user");
        mysqlHost = plugin.getConfig().getString("mysql.host");
        mysqlPort = plugin.getConfig().getInt("mysql.port");
    }
}
