package de.jozelot.jozelotUtils;

import de.jozelot.jozelotUtils.commands.*;
import de.jozelot.jozelotUtils.database.RedisListener;
import de.jozelot.jozelotUtils.database.RedisManager;
import de.jozelot.jozelotUtils.database.RedisSetup;
import de.jozelot.jozelotUtils.listener.*;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import de.jozelot.jozelotUtils.utils.ConsoleLogger;
import de.jozelot.jozelotUtils.utils.ReloadPlugin;
import org.apache.commons.codec.language.bm.Lang;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.ServerTickManager;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Map;

public final class JozelotUtils extends JavaPlugin {

    private ConfigManager config;
    private RedisSetup redisSetup;
    private RedisManager redisManager;
    private LangManager lang;
    private ReloadPlugin reloadPlugin;
    private PlayerNameTag playerNameTag;
    private ConsoleLogger consoleLogger;

    @Override
    public void onEnable() {
        this.config = new ConfigManager(this);
        this.lang = new LangManager(this);
        this.lang.load();

        this.redisSetup = new RedisSetup(this);
        redisSetup.setup();
        this.redisManager = new RedisManager(this);
        this.reloadPlugin = new ReloadPlugin(this);
        this.consoleLogger = new ConsoleLogger(this);

        new RedisListener(this);

        Map<String, String> redisData = redisManager.fetchLanguageData();
        if (redisData != null) {
            this.lang.integrateRedisData(redisData);
            Bukkit.getConsoleSender().sendMessage("§a[§6JoUtils§a] §7Sprach-Synchronisierung mit Proxy abgeschlossen!");
        }

        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("fly").setTabCompleter(new FlyCommandTab());
        getCommand("flyspeed").setExecutor(new FlySpeedCommand(this));
        getCommand("flyspeed").setTabCompleter(new FlySpeedCommandTab());
        getCommand("spec").setExecutor(new SpecCommand(this));
        getCommand("heal").setExecutor(new HealCommand(this));

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new LeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new GriefPrevention(this), this);
        getServer().getPluginManager().registerEvents(new WorldSettings(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new FallOffListener(this), this);

        playerNameTag = new PlayerNameTag(this);
        getServer().getPluginManager().registerEvents(playerNameTag, this);
        applyGameRules();

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

        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§a Minecraft läuft in der " + Bukkit.getBukkitVersion());
        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§a ----------------------------------------------");
        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§a    +==================+");
        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§a    |      JoUtils     |");
        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§a    +==================+");
        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§a ----------------------------------------------");
        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§6    Version: §e" + getVersion());
        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§a ----------------------------------------------");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public String getVersion() {
        return getDescription().getVersion();
    }
    public ConfigManager getConfigManager() {
        return config;
    }
    public RedisSetup getRedisSetup() {
        return redisSetup;
    }
    public RedisManager getRedisManager() {
        return redisManager;
    }
    public LangManager getLang() {
        return lang;
    }
    public ReloadPlugin getReloadPlugin() {
        return reloadPlugin;
    }
    public PlayerNameTag getPlayerNameTag() {
        return playerNameTag;
    }

    public void applyGameRules() {
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, config.isKeepInventory());
            world.setGameRule(GameRule.DO_FIRE_TICK, config.isFireSpread());
            world.setGameRule(GameRule.NATURAL_REGENERATION, config.isNaturalRegeneration());
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, config.isDaylightCycle());
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, config.isWeatherCycle());
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, config.isAnnounceAdvancements());
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, config.getTickSpeed());
            world.setGameRule(GameRule.LOCATOR_BAR, config.isLocatorBar());

            world.setGameRule(GameRule.MOB_GRIEFING, config.isMobGriefing());
            world.setGameRule(GameRule.DO_INSOMNIA, config.isDoInsomnia());
            world.setGameRule(GameRule.DO_TILE_DROPS, !config.isBlockDrops());
            world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, config.getPlayerSleepingPercentage());
            world.setGameRule(GameRule.DO_PATROL_SPAWNING, config.isSpawnPatrols());
            world.setGameRule(GameRule.DO_TRADER_SPAWNING, config.isSpawnWanderingTrader());

            if (!config.isDaylightCycle()) {
                world.setTime(6000L);
            }
        }
    }

    public void freezeGame(boolean state) {
        ServerTickManager serverTickManager = Bukkit.getServerTickManager();
        serverTickManager.setFrozen(state);
    }

    public ConsoleLogger getConsoleLogger() {
        return consoleLogger;
    }

}
