package de.jozelot.jozelotProxy.commands.admin;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClearChatCommand implements SimpleCommand {

    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final ConfigManager config;
    private final ProxyServer server;
    private final JozelotProxy plugin;
    MiniMessage mm = MiniMessage.miniMessage();

    public ClearChatCommand(JozelotProxy plugin) {
        this.lang = plugin.getLang();
        this.server = plugin.getServer();
        this.consoleLogger = plugin.getConsoleLogger();
        this.config = plugin.getConfig();
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("network.command.clearchat")) {
            invocation.source().sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }
        CommandSource source = invocation.source();
        String name = (invocation.source() instanceof Player player) ? player.getUsername() : "Konsole";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 250; i++) {
            sb.append("\n");
        }
        String clearBlock = sb.toString();

        List<String> successMessage = lang.formatList("chat-cleared", Map.of("player-name", name));

        for (Player player : server.getAllPlayers()) {
            if (player.hasPermission("network.clearchat.bypass")) {
                continue;
            }
            player.sendMessage(mm.deserialize(clearBlock));

            for (String line : successMessage) {
                player.sendMessage(mm.deserialize(line));
            }
        }

        consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + name + "<" + config.getColorPrimary() + "> hat den Chat gelehrt");
        for (Player player : server.getAllPlayers()) {
            if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                player.sendMessage(mm.deserialize(lang.format("chat-cleared-admin", Map.of("player-name", name))));
            }
        }
        source.sendMessage(mm.deserialize(lang.format("chat-cleared-success", null)));

        UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
        plugin.getMySQLManager().logAction(operatorUUID, "CHATCLEAR", "server:all", "");
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("network.command.clearchat");
    }

}
