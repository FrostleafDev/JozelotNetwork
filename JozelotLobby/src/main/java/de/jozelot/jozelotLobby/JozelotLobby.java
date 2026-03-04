package de.jozelot.jozelotLobby;

import de.jozelot.jozelotLobby.api.placeholderapi.PlayerCount;
import de.jozelot.jozelotLobby.commands.BlockHiddenCommands;
import de.jozelot.jozelotLobby.commands.OpenMenu;
import de.jozelot.jozelotLobby.commands.SecretCommand;
import de.jozelot.jozelotLobby.database.*;
import de.jozelot.jozelotLobby.secrets.engine.SecretDatabase;
import de.jozelot.jozelotLobby.secrets.engine.SecretListener;
import de.jozelot.jozelotLobby.secrets.engine.SecretManager;
import de.jozelot.jozelotLobby.utils.ScoreboardManager;
import de.jozelot.jozelotLobby.ui.items.ClickHandler;
import de.jozelot.jozelotLobby.ui.items.HotbarItems;
import de.jozelot.jozelotLobby.ui.items.HotbarManager;
import de.jozelot.jozelotLobby.player.LobbyPlayerDatabase;
import de.jozelot.jozelotLobby.player.LobbyPlayerManager;
import de.jozelot.jozelotLobby.player.PlayerConnectionListener;
import de.jozelot.jozelotLobby.storage.ConfigManager;
import de.jozelot.jozelotLobby.storage.LangManager;
import de.jozelot.jozelotLobby.utils.NetworkStateManager;
import de.jozelot.jozelotLobby.utils.ReloadPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class JozelotLobby extends JavaPlugin {

    private ConfigManager config;
    private RedisSetup redisSetup;
    private RedisManager redisManager;
    private RedisPublish redisPublish;
    private LangManager lang;
    private ReloadPlugin reloadPlugin;
    private MySQLSetup mySQLSetup;
    private MySQLManager mySQLManager;

    private HotbarItems hotbarItems;
    private HotbarManager hotbarManager;

    private LobbyPlayerManager lobbyPlayerManager;
    private LobbyPlayerDatabase lobbyPlayerDatabase;
    private NetworkStateManager networkStateManager;
    private ScoreboardManager scoreboardManager;

    private SecretDatabase secretDb;
    private SecretManager secretMgr;

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
        this.mySQLManager = new MySQLManager(this);
        this.secretDb = new SecretDatabase(this);
        this.secretMgr = new SecretManager(this);
        this.redisPublish = new RedisPublish(this);

        this.reloadPlugin = new ReloadPlugin(this);

        new RedisListener(this);
        this.lobbyPlayerDatabase = new LobbyPlayerDatabase(this);
        this.lobbyPlayerManager = new LobbyPlayerManager(this);
        this.hotbarItems = new HotbarItems(this);
        this.hotbarManager = new HotbarManager(this);

        this.networkStateManager = new NetworkStateManager();
        this.scoreboardManager = new ScoreboardManager(this);
        this.scoreboardManager.startScheduler();

        getCommand("openmenu").setExecutor(new OpenMenu(this));
        SecretCommand secretCommand = new SecretCommand(this);
        getCommand("secrets").setExecutor(secretCommand);
        getCommand("secrets").setTabCompleter(secretCommand);

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ClickHandler(this), this);
        getServer().getPluginManager().registerEvents(new BlockHiddenCommands(), this);
        getServer().getPluginManager().registerEvents(new SecretListener(this), this);

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

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlayerCount(this).register();
            getLogger().info("PlaceholderAPI Expansion registriert!");
        }
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

    public MySQLManager getMySQLManager() {
        return mySQLManager;
    }

    public LobbyPlayerDatabase getLobbyPlayerDatabase() {
        return lobbyPlayerDatabase;
    }

    public NetworkStateManager getNetworkStateManager() {
        return networkStateManager;
    }

    public SecretDatabase getSecretDb() {
        return secretDb;
    }

    public SecretManager getSecretMgr() {
        return secretMgr;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
