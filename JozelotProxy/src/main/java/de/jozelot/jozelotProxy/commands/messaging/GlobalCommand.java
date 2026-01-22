package de.jozelot.jozelotProxy.commands.messaging;

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

public class GlobalCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final ConfigManager config;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public GlobalCommand(JozelotProxy plugin) {
        this.server = plugin.getServer();
        this.lang = plugin.getLang();
        this.plugin = plugin;
        this.consoleLogger = plugin.getConsoleLogger();
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("network.command.global")) {
            invocation.source().sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(mm.deserialize(lang.getOnlyPlayer()));
            return;
        }

        String[] args = invocation.arguments();

        if (args.length < 1) {
            player.sendMessage(mm.deserialize(lang.format("command-global-usage", Map.of())));
            return;
        }

        String rawMessage = String.join(" ", args);

        String processedMessage = rawMessage;
        if (!player.hasPermission("network.chat.minimessage")) {
            processedMessage = mm.escapeTags(rawMessage);
        }

        String sendFormat = lang.format("global-chat-format", Map.of(
                "rank-prefix", plugin.getLuckpermsUtils().getPlayerPrefix(player),
                "player-name", player.getUsername(),
                "message", processedMessage
        ));

        server.getAllPlayers().forEach(p -> p.sendMessage(mm.deserialize(sendFormat)));

        consoleLogger.broadCastToConsole("[GLOBAL-CHAT] " + player.getUsername() + ": " + rawMessage);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.global");
    }
}