package de.jozelot.jozelotLobby;

import de.jozelot.jozelotLobby.database.RedisListener;
import de.jozelot.jozelotLobby.database.RedisManager;
import de.jozelot.jozelotLobby.database.RedisSetup;
import de.jozelot.jozelotLobby.items.HotbarItems;
import de.jozelot.jozelotLobby.items.HotbarManager;
import de.jozelot.jozelotLobby.player.LobbyPlayerManager;
import de.jozelot.jozelotLobby.storage.ConfigManager;
import de.jozelot.jozelotLobby.storage.LangManager;
import de.jozelot.jozelotLobby.utils.ReloadPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class JozelotLobby extends JavaPlugin {

    private ConfigManager config;
    private RedisSetup redisSetup;
    private RedisManager redisManager;
    private LangManager lang;
    private ReloadPlugin reloadPlugin;

    private HotbarItems hotbarItems;
    private HotbarManager hotbarManager;

    private LobbyPlayerManager lobbyPlayerManager;

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

        this.hotbarItems = new HotbarItems(this);
        this.hotbarManager = new HotbarManager(this);
        this.lobbyPlayerManager = new LobbyPlayerManager(this);

        getServer().getPluginManager().registerEvents(hotbarManager, this);

        Map<String, String> redisData = redisManager.fetchLanguageData();
        if (redisData != null) {
            this.lang.integrateRedisData(redisData);
            Bukkit.getConsoleSender().sendMessage("§a[§JoLobby§a] §7Sprach-Synchronisierung mit Proxy abgeschlossen!");
        }


        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§a Minecraft läuft in der " + Bukkit.getBukkitVersion());
        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§a ----------------------------------------------");
        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§a    +==================+");
        getServer().getConsoleSender().sendMessage("§a[§6JoUtils§a]§a    |      JoLobby     |");
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

    public ReloadPlugin getReloadPlugin() {
        return reloadPlugin;
    }

    public LangManager getLang() {
        return lang;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public RedisSetup getRedisSetup() {
        return redisSetup;
    }

    public ConfigManager getConfigManager() {
        return config;
    }

    public HotbarItems getHotbarItems() {
        return hotbarItems;
    }

    public HotbarManager getHotbarManager() {
        return hotbarManager;
    }

    public LobbyPlayerManager getLobbyPlayerManager() {
        return lobbyPlayerManager;
    }
}
