package de.jozelot.jozelotProxy.utils;

import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ConsoleLogger {

    private final ProxyServer server;
    private final ConfigManager config;

    public ConsoleLogger(JozelotProxy plugin) {
        this.server = plugin.getServer();
        this.config = plugin.getConfig();
    }

    /**
     * Just a generell logger for my plugin, so you can see in the console
     * when my plugin sends a message
     */
    public void broadCastToConsole(String message) {
        server.getConsoleCommandSource().sendMessage(
                MiniMessage.miniMessage().deserialize("<" + config.getColorGrey() + ">[<gradient:#f90036:#f90011><b>JozelotProxy<" + config.getColorGrey() + ">] <reset>" + message)
        );
    }
}
