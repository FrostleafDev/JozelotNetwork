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
import de.jozelot.jozelotProxy.commands.ClearChatCommand;
import de.jozelot.jozelotProxy.commands.FindCommand;
import de.jozelot.jozelotProxy.commands.LobbyCommand;
import de.jozelot.jozelotProxy.commands.NetworkCommand;
import de.jozelot.jozelotProxy.listener.JoinListeners;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import de.jozelot.jozelotProxy.utils.PlayerSends;
import de.jozelot.jozelotProxy.utils.PluginReload;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.nio.file.Path;

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

    private MiniMessage mm = MiniMessage.miniMessage();

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
        this.pluginReload = new PluginReload(this);
        this.consoleLogger = new ConsoleLogger(this);
        logger.info("Utils geladen");
        consoleLogger.broadCastToConsole("Plugin Logger gestartet");

        // Commands
        CommandManager cm = server.getCommandManager();
        CommandMeta hubMeta = cm.metaBuilder("hub").aliases("lobby", "l").build();
        CommandMeta networkMeta = cm.metaBuilder("network").aliases("net").build();
        CommandMeta clearchatMeta = cm.metaBuilder("clearchat").build();
        CommandMeta findMeta = cm.metaBuilder("find").build();

        cm.register(hubMeta, new LobbyCommand(this));
        cm.register(networkMeta, new NetworkCommand(this));
        cm.register(clearchatMeta, new ClearChatCommand(this));
        cm.register(findMeta, new FindCommand(this));
        consoleLogger.broadCastToConsole("Commands erstellt");

        // Listener
        server.getEventManager().register(this, new JoinListeners());
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

    public String getVersion() {
        return server.getPluginManager().getPlugin("jozelotproxy")
                .flatMap(container -> container.getDescription().getVersion())
                .orElse("Unbekannt");
    }
}
