package de.jozelot.jozelotUtils.utils;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ConsoleLogger {

    private final JozelotUtils plugin;
    private final ConfigManager config;

    public ConsoleLogger(JozelotUtils plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    /**
     * Just a generell logger for my plugin, so you can see in the console
     * when my plugin sends a message
     * @param message
     */
    public void broadCastToConsole(String message) {
        plugin.getServer().getConsoleSender().sendMessage(
                MiniMessage.miniMessage().deserialize("<" + config.getColorGrey() + ">[<gradient:#f90036:#f90011><b>JozelotUtils<" + config.getColorGrey() + ">] <reset>" + message)
        );
    }
}
