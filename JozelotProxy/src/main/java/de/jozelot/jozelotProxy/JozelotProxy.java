package de.jozelot.jozelotProxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.apis.GroupManager;
import de.jozelot.jozelotProxy.apis.PteroManager;
import de.jozelot.jozelotProxy.commands.*;
import de.jozelot.jozelotProxy.commands.admin.*;
import de.jozelot.jozelotProxy.commands.messaging.GMsgCommand;
import de.jozelot.jozelotProxy.commands.messaging.GlobalCommand;
import de.jozelot.jozelotProxy.commands.messaging.MsgCommand;
import de.jozelot.jozelotProxy.commands.messaging.ReplyCommand;
import de.jozelot.jozelotProxy.database.MySQLManager;
import de.jozelot.jozelotProxy.database.MySQLSetup;
import de.jozelot.jozelotProxy.database.RedisSetup;
import de.jozelot.jozelotProxy.listener.*;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.*;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Plugin(
        id = "jozelotproxy",
        name = "JozelotProxy",
        version = "1.0.0",
        description = "Velocity Proxy Plugin für jozelot.de - Bereitgestellt von jozelot_",
        url = "https://jozelot.de", authors = {"jozelot_"}
)

public class JozelotProxy {

    // Objects
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private ConfigManager config;
    private LangManager lang;
    private PlayerSends playerSends;
    private PluginReload pluginReload;
    private ConsoleLogger consoleLogger;
    private PteroManager pteroManager;
    private GroupManager groupManager;
    private LuckPerms luckPerms;
    private LuckpermsUtils luckpermsUtils;
    private BrandNameChanger brandNameChanger;

    private PlaytimeListener playtimeListener;

    private RedisSetup redisSetup;
    private MySQLSetup mySQLSetup;
    private MySQLManager mySQLManager;

    private MiniMessage mm = MiniMessage.miniMessage();
    public record ReplyData(UUID partnerId, boolean isGlobal) {}

    private final Map<UUID, ReplyData> replyMap = new HashMap<>();

    /***
     * Plugin start
     * @param server Proxy Server halt
     * @param logger Logger to log stuff
     * @param dataDirectory Directory of the plugin config and files
     */
    @Inject
    public JozelotProxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        logger.info("Proxy wird gestartet...");
    }

    /***
     * Server start
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.config = new ConfigManager(dataDirectory, "config.yml");
        this.config.loadDefaultConfig(getClass().getResourceAsStream("/config.yml"));
        logger.info("Config geladen");

        this.lang = new LangManager(this, dataDirectory, "lang.yml");
        lang.loadDefaultLang(getClass().getResourceAsStream("/lang.yml"));
        logger.info("Sprachdatei geladen");

        this.playerSends = new PlayerSends(this);
        this.consoleLogger = new ConsoleLogger(this);
        this.pteroManager = new PteroManager(this);
        logger.info("Utils geladen");
        consoleLogger.broadCastToConsole("Plugin Logger gestartet");

        this.redisSetup = new RedisSetup(this);
        redisSetup.setup();
        this.mySQLSetup = new MySQLSetup(this);
        mySQLSetup.setup();
        this.mySQLManager = new MySQLManager(this);
        mySQLManager.createTables();
        mySQLManager.registerAllServers(server.getAllServers());
        this.groupManager = new GroupManager(this);
        groupManager.load();
        this.pluginReload = new PluginReload(this);

        if (server.getPluginManager().getPlugin("luckperms").isPresent()) {
            this.luckPerms = LuckPermsProvider.get();
        }
        this.luckpermsUtils = new LuckpermsUtils(this);
        brandNameChanger = new BrandNameChanger();

        // Commands
        CommandManager cm = server.getCommandManager();
        CommandMeta hubMeta = cm.metaBuilder("hub").aliases("lobby", "l").build();
        CommandMeta networkMeta = cm.metaBuilder("network").aliases("net").build();
        CommandMeta clearchatMeta = cm.metaBuilder("clearchat").build();
        CommandMeta findMeta = cm.metaBuilder("find").build();
        CommandMeta banMeta = cm.metaBuilder("ban").build();
        CommandMeta unbanMeta = cm.metaBuilder("unban").aliases("pardon").build();
        CommandMeta banlistMeta = cm.metaBuilder("banlist").aliases("bans").build();
        CommandMeta kickMeta = cm.metaBuilder("kick").build();
        CommandMeta tpoMeta = cm.metaBuilder("tpo").aliases("tpto").build();
        CommandMeta tpohereMeta = cm.metaBuilder("tpohere").build();
        CommandMeta serverMeta = cm.metaBuilder("server").build();
        CommandMeta sendMeta = cm.metaBuilder("send").aliases("move").build();
        CommandMeta gmsgMeta = cm.metaBuilder("gmsg").aliases("globalmsg").build();
        CommandMeta msgMeta = cm.metaBuilder("msg").aliases("tell").build();
        CommandMeta replyMeta = cm.metaBuilder("reply").aliases("r").build();
        CommandMeta globalMeta = cm.metaBuilder("global").aliases("g").build();
        CommandMeta whitelistMeta = cm.metaBuilder("whitelist").build();
        CommandMeta playtimeMeta = cm.metaBuilder("playtime").build();
        CommandMeta broadcastMeta = cm.metaBuilder("broadcast").build();

        cm.register(hubMeta, new LobbyCommand(this));
        cm.register(networkMeta, new NetworkCommand(this));
        cm.register(clearchatMeta, new ClearChatCommand(this));
        cm.register(findMeta, new FindCommand(this));
        cm.register(banMeta, new BanCommand(this));
        cm.register(unbanMeta, new UnbanCommand(this));
        cm.register(banlistMeta, new BanListCommand(this));
        cm.register(kickMeta, new KickCommand(this));
        cm.register(tpoMeta, new TpoCommand(this));
        cm.register(tpohereMeta, new TpoHereCommand(this));
        cm.register(serverMeta, new ServerCommand(this));
        cm.register(sendMeta, new SendCommand(this));
        cm.register(gmsgMeta, new GMsgCommand(this));
        cm.register(msgMeta, new MsgCommand(this));
        cm.register(replyMeta, new ReplyCommand(this));
        cm.register(globalMeta, new GlobalCommand(this));
        cm.register(whitelistMeta, new WhitelistCommand(this));
        cm.register(playtimeMeta, new PlaytimeCommand(this));
        cm.register(broadcastMeta, new BroadcastCommand(this));
        consoleLogger.broadCastToConsole("Commands erstellt");

        this.playtimeListener = new PlaytimeListener(this);

        // Listener

        server.getEventManager().register(this, new ServerSwitchListener(this));
        server.getEventManager().register(this, new ProxyPingListener(this));
        server.getEventManager().register(this, new GroupChatListener(this));
        server.getEventManager().register(this, playtimeListener);
        consoleLogger.broadCastToConsole("Listener erstellt");

        consoleLogger.broadCastToConsole( "<" + config.getColorPrimary() + ">----------------------------------------------");
        consoleLogger.broadCastToConsole( "<" + config.getColorPrimary() + ">Velocity läuft in der <" + config.getColorSecondary() + ">" + server.getVersion().getVersion());
        consoleLogger.broadCastToConsole( "<" + config.getColorPrimary() + ">----------------------------------------------");
        consoleLogger.broadCastToConsole( "<" + config.getColorPrimary() + ">   +==================+");
        consoleLogger.broadCastToConsole( "<" + config.getColorPrimary() + ">   |  JozelotNetzwork |");
        consoleLogger.broadCastToConsole( "<" + config.getColorPrimary() + ">   +==================+");
        consoleLogger.broadCastToConsole( "<" + config.getColorPrimary() + ">----------------------------------------------");
        consoleLogger.broadCastToConsole( "<" + config.getColorPrimary() + ">    Version: <" + config.getColorSecondary() + ">" + getVersion());
        consoleLogger.broadCastToConsole( "<" + config.getColorPrimary() + ">----------------------------------------------");
    }

    /***
     * Plugin shutdown
     */
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        consoleLogger.broadCastToConsole("Proxy wird beendet, schließe Verbindungen...");
        server.getAllPlayers().forEach(p -> playtimeListener.saveAndRemoveSession(p));
    }

    public ProxyServer getServer() {
        return server;
    }

    public ConfigManager getConfig() {
        return config;
    }

    public LangManager getLang() {
        return lang;
    }

    public PlayerSends getPlayerSends() {
        return playerSends;
    }

    public PluginReload getPluginReload() {
        return pluginReload;
    }

    public ConsoleLogger getConsoleLogger() {
        return consoleLogger;
    }

    public RedisSetup getRedisSetup() {
        return redisSetup;
    }

    public MySQLSetup getMySQLSetup() {
        return mySQLSetup;
    }

    public MySQLManager getMySQLManager() {
        return mySQLManager;
    }

    public PteroManager getPteroManager() {
        return pteroManager;
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }

    public LuckPerms getLuckPerms() {
        return luckPerms;
    }

    public LuckpermsUtils getLuckpermsUtils() {
        return luckpermsUtils;
    }

    public BrandNameChanger getBrandNameChanger() {
        return brandNameChanger;
    }

    public Map<UUID, ReplyData> getReplyMap() {
        return replyMap;
    }

    public PlaytimeListener getPlaytimeListener() {
        return playtimeListener;
    }

    public String getVersion() {
        return server.getPluginManager().getPlugin("jozelotproxy")
                .flatMap(container -> container.getDescription().getVersion())
                .orElse("Unbekannt");
    }
}
