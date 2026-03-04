package de.jozelot.jozelotUtils;

import de.jozelot.jozelotUtils.commands.*;
import de.jozelot.jozelotUtils.database.MySQLSetup;
import de.jozelot.jozelotUtils.database.RedisListener;
import de.jozelot.jozelotUtils.database.RedisManager;
import de.jozelot.jozelotUtils.database.RedisSetup;
import de.jozelot.jozelotUtils.listener.*;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import de.jozelot.jozelotUtils.utils.ConsoleLogger;
import de.jozelot.jozelotUtils.utils.ReloadPlugin;
import de.jozelot.jozelotUtils.utils.VanishManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Map;

public final class JozelotUtils extends JavaPlugin {

    private static BukkitAudiences adventure;

    private ConfigManager config;
    private RedisSetup redisSetup;
    private RedisManager redisManager;
    private MySQLSetup mySQLSetup;
    private LangManager lang;
    private ReloadPlugin reloadPlugin;
    private PlayerNameTag playerNameTag;
    private ConsoleLogger consoleLogger;
    private VanishManager vanishManager;

    @Override
    public void onEnable() {
        // 1. Adventure Initialisierung (Muss zuerst kommen!)
        adventure = BukkitAudiences.create(this);

        this.config = new ConfigManager(this);
        this.lang = new LangManager(this);
        this.lang.load();

        this.mySQLSetup = new MySQLSetup(this);
        this.vanishManager = new VanishManager(this);

        this.redisSetup = new RedisSetup(this);
        redisSetup.setup();
        this.redisManager = new RedisManager(this);
        this.reloadPlugin = new ReloadPlugin(this);
        this.consoleLogger = new ConsoleLogger(this);

        new RedisListener(this);

        Map<String, String> redisData = redisManager.fetchLanguageData();
        if (redisData != null) {
            this.lang.integrateRedisData(redisData);
            Bukkit.getConsoleSender().sendMessage("§a[§6JoUtils§a] §7Sprach-Synchronisierung mit Proxy abgeschlossen.");
        }

        registerCommands();
        registerListeners();

        playerNameTag = new PlayerNameTag(this);
        getServer().getPluginManager().registerEvents(playerNameTag, this);
        applyGameRules();

        setupDynamicCommands();

        printStartupMessage();
    }

    @Override
    public void onDisable() {
        // Adventure ordnungsgemäß schließen
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }

    /**
     * Statische Zugriffsmethode für den Adventure-Provider.
     */
    public static BukkitAudiences adventure() {
        if (adventure == null) {
            throw new IllegalStateException("Adventure wurde aufgerufen, während das Plugin deaktiviert war.");
        }
        return adventure;
    }

    private void registerCommands() {
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("fly").setTabCompleter(new FlyCommandTab());
        getCommand("flyspeed").setExecutor(new FlySpeedCommand(this));
        getCommand("flyspeed").setTabCompleter(new FlySpeedCommandTab());
        getCommand("spec").setExecutor(new SpecCommand(this));
        getCommand("heal").setExecutor(new HealCommand(this));
    }

    private void registerListeners() {
        var pm = getServer().getPluginManager();
        pm.registerEvents(new JoinListener(this), this);
        pm.registerEvents(new LeaveListener(this), this);
        pm.registerEvents(new GriefPrevention(this), this);
        pm.registerEvents(new WorldSettings(this), this);
        pm.registerEvents(new PlayerChatListener(this), this);
        pm.registerEvents(new FallOffListener(this), this);
        pm.registerEvents(new VanishListener(this), this);
    }

    private void setupDynamicCommands() {
        try {
            if (config.isSpawnCommand()) {
                Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

                commandMap.register("jozelotutils", new SpawnCommand(this));
                commandMap.register("jozelotutils", new SetSpawnCommand(this));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printStartupMessage() {
        var cs = getServer().getConsoleSender();
        cs.sendMessage("§a[§6JoUtils§a] §7Umgebung: " + Bukkit.getBukkitVersion());
        cs.sendMessage("§a[§6JoUtils§a] §7Plugin-Version: §e" + getVersion());
        cs.sendMessage("§a[§6JoUtils§a] §2Plugin erfolgreich geladen.");
    }

    // Getter
    public String getVersion() { return getDescription().getVersion(); }
    public ConfigManager getConfigManager() { return config; }
    public RedisSetup getRedisSetup() { return redisSetup; }
    public RedisManager getRedisManager() { return redisManager; }
    public LangManager getLang() { return lang; }
    public ReloadPlugin getReloadPlugin() { return reloadPlugin; }
    public PlayerNameTag getPlayerNameTag() { return playerNameTag; }
    public ConsoleLogger getConsoleLogger() { return consoleLogger; }
    public VanishManager getVanishManager() { return vanishManager; }
    public MySQLSetup getMySQLSetup() { return mySQLSetup; }

    public void applyGameRules() {
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, config.isKeepInventory());
            world.setGameRule(GameRule.DO_FIRE_TICK, config.isFireSpread());
            world.setGameRule(GameRule.NATURAL_REGENERATION, config.isNaturalRegeneration());
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, config.isDaylightCycle());
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, config.isWeatherCycle());
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, config.isAnnounceAdvancements());
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, config.getTickSpeed());
            world.setGameRule(GameRule.MOB_GRIEFING, config.isMobGriefing());
            world.setGameRule(GameRule.DO_INSOMNIA, config.isDoInsomnia());
            world.setGameRule(GameRule.DO_TILE_DROPS, !config.isBlockDrops());
            world.setGameRule(GameRule.DO_PATROL_SPAWNING, config.isSpawnPatrols());
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, config.isSpawnWanderingTrader());

            if (!config.isDaylightCycle()) {
                world.setTime(6000L);
            }
        }
    }
}