package de.jozelot.jozelotLobby;

import de.jozelot.jozelotLobby.database.*;
import de.jozelot.jozelotLobby.ui.inventories.InventoryManager;
import de.jozelot.jozelotLobby.ui.items.ClickHandler;
import de.jozelot.jozelotLobby.ui.items.HotbarItems;
import de.jozelot.jozelotLobby.ui.items.HotbarManager;
import de.jozelot.jozelotLobby.player.LobbyPlayerDatabase;
import de.jozelot.jozelotLobby.player.LobbyPlayerManager;
import de.jozelot.jozelotLobby.player.PlayerConnectionListener;
import de.jozelot.jozelotLobby.storage.ConfigManager;
import de.jozelot.jozelotLobby.storage.LangManager;
import de.jozelot.jozelotLobby.utils.ReloadPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.N;

import java.util.Map;

public final class JozelotLobby extends JavaPlugin {

    private ConfigManager config;
    private RedisSetup redisSetup;
    private RedisManager redisManager;
    private RedisPublish redisPublish;
    private LangManager lang;
    private ReloadPlugin reloadPlugin;

    private HotbarItems hotbarItems;
    private HotbarManager hotbarManager;

    private LobbyPlayerManager lobbyPlayerManager;
    private LobbyPlayerDatabase lobbyPlayerDatabase;

    private MySQLSetup mySQLSetup;
    private InventoryManager inventoryManager;

    private NetworkStateManager networkStateManager;

    @Override
    public void onEnable() {
        this.config = new ConfigManager(this);
        this.lang = new LangManager(this);
        this.lang.load();


        this.redisSetup = new RedisSetup(this);
        redisSetup.setup();
        this.redisManager = new RedisManager(this);
        this.mySQLSetup = new MySQLSetup(this);
        mySQLSetup.setup();
        this.reloadPlugin = new ReloadPlugin(this);
        this.redisPublish = new RedisPublish(this);

        new RedisListener(this);
        this.lobbyPlayerDatabase = new LobbyPlayerDatabase(this);
        this.lobbyPlayerManager = new LobbyPlayerManager(this);
        this.hotbarItems = new HotbarItems(this);
        this.hotbarManager = new HotbarManager(this);

        this.inventoryManager = new InventoryManager(this);
        this.networkStateManager = new NetworkStateManager();

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ClickHandler(this), this);

        Map<String, String> redisData = redisManager.fetchLanguageData();
        if (redisData != null) {
            this.lang.integrateRedisData(redisData);
            Bukkit.getConsoleSender().sendMessage("§a[§JoLobby§a] §7Sprach-Synchronisierung mit Proxy abgeschlossen!");
        }


        getServer().getConsoleSender().sendMessage("§a[§JoLobby§a]§a Minecraft läuft in der " + Bukkit.getBukkitVersion());
        getServer().getConsoleSender().sendMessage("§a[§JoLobby§a]§a ----------------------------------------------");
        getServer().getConsoleSender().sendMessage("§a[§JoLobby§a]§a    +==================+");
        getServer().getConsoleSender().sendMessage("§a[§JoLobby§a]§a    |      JoLobby     |");
        getServer().getConsoleSender().sendMessage("§a[§JoLobby§a]§a    +==================+");
        getServer().getConsoleSender().sendMessage("§a[§JoLobby§a]§a ----------------------------------------------");
        getServer().getConsoleSender().sendMessage("§a[§JoLobby§a]§6    Version: §e" + getVersion());
        getServer().getConsoleSender().sendMessage("§a[§JoLobby§a]§a ----------------------------------------------");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        lobbyPlayerDatabase.saveAllPlayerSettings(lobbyPlayerManager.getAllPlayers());
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
    public RedisPublish getRedisPublish() {
        return redisPublish;
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

    public MySQLSetup getMySQLSetup() {
        return mySQLSetup;
    }

    public LobbyPlayerDatabase getLobbyPlayerDatabase() {
        return lobbyPlayerDatabase;
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public NetworkStateManager getNetworkStateManager() {
        return networkStateManager;
    }
}
