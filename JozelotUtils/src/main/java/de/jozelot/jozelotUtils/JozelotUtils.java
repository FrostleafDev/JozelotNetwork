package de.jozelot.jozelotUtils;

import de.jozelot.jozelotUtils.commands.FlyCommand;
import de.jozelot.jozelotUtils.commands.FlyCommandTab;
import de.jozelot.jozelotUtils.commands.FlySpeedCommand;
import de.jozelot.jozelotUtils.commands.FlySpeedCommandTab;
import de.jozelot.jozelotUtils.database.RedisListener;
import de.jozelot.jozelotUtils.database.RedisManager;
import de.jozelot.jozelotUtils.database.RedisSetup;
import de.jozelot.jozelotUtils.listener.GriefPrevention;
import de.jozelot.jozelotUtils.listener.JoinListener;
import de.jozelot.jozelotUtils.listener.PlayerNameTag;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import de.jozelot.jozelotUtils.utils.ReloadPlugin;
import org.apache.commons.codec.language.bm.Lang;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class JozelotUtils extends JavaPlugin {

    private ConfigManager config;
    private RedisSetup redisSetup;
    private RedisManager redisManager;
    private LangManager lang;
    private ReloadPlugin reloadPlugin;
    private PlayerNameTag playerNameTag;

    @Override
    public void onEnable() {
        this.config = new ConfigManager(this);
        this.lang = new LangManager(this);
        this.lang.load();

        this.redisSetup = new RedisSetup(this);
        redisSetup.setup();
        this.redisManager = new RedisManager(this);
        this.reloadPlugin = new ReloadPlugin(this);

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

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        playerNameTag = new PlayerNameTag(this);
        getServer().getPluginManager().registerEvents(playerNameTag, this);
        getServer().getPluginManager().registerEvents(new GriefPrevention(this), this);

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
}
