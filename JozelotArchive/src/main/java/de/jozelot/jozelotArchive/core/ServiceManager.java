package de.jozelot.jozelotArchive.core;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.core.database.redis.RedisConnection;
import de.jozelot.jozelotArchive.core.database.redis.RedisListener;
import de.jozelot.jozelotArchive.core.database.redis.RedisManager;
import de.jozelot.jozelotArchive.core.database.sql.SQLConnection;
import de.jozelot.jozelotArchive.player.user.UserManager;
import de.jozelot.jozelotArchive.storage.ConfigManager;
import de.jozelot.jozelotArchive.storage.FileSystemManager;
import de.jozelot.jozelotArchive.storage.LangManager;
import org.bukkit.Bukkit;

import java.util.logging.Level;

public class ServiceManager {

    private final JozelotArchive plugin;

    private ConfigManager configManager;
    private LangManager langManager;
    private SQLConnection sqlConnection;
    private RedisConnection redisConnection;
    private RedisManager redisManager;
    private RedisListener redisListener;
    private PluginReload pluginReload;
    private FileSystemManager fileSystemManager;

    private UserManager userManager;

    public ServiceManager(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if every plugin dependency of the plugin is loaded
     * @return True for yes ; False for no
     */
    public boolean checkForDependencies() {
        var pm = Bukkit.getPluginManager();

        if (pm.getPlugin("LuckPerms") == null) {
            plugin.getLogger().log(Level.SEVERE, "LuckPerms ist nicht vorhanden!");
            plugin.getLogger().log(Level.SEVERE, "Plugin start fehlgeschlagen...");
            return false;
        }

        if (pm.getPlugin("FastAsyncWorldEdit") == null && pm.getPlugin("WorldEdit") == null) {
            plugin.getLogger().log(Level.SEVERE, "Es ist keine WorldEdit Version vorhanden!");
            plugin.getLogger().log(Level.SEVERE, "Plugin start fehlgeschlagen...");
            return false;
        }

        return true;
    }

    public void initialize() {
        this.configManager = new ConfigManager(plugin);
        this.langManager = new LangManager(plugin);
        this.sqlConnection = new SQLConnection(plugin);
        this.redisConnection = new RedisConnection(plugin);
        this.redisManager = new RedisManager(plugin);
        this.pluginReload = new PluginReload(plugin, this);
        this.fileSystemManager = new FileSystemManager(plugin);
        this.userManager = new UserManager(plugin);
        this.redisListener = new RedisListener(plugin);
    }

    public void shutdown() {
        this.sqlConnection.close();
        this.redisConnection.close();
    }

    public void reload() {
        pluginReload.reload();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public SQLConnection getSQLConnection() {
        return sqlConnection;
    }

    public RedisConnection getRedisConnection() {
        return redisConnection;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public PluginReload getPluginReload() {
        return pluginReload;
    }

    public UserManager getUserManager() {
        return userManager;
    }
}
