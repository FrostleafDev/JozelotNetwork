package de.jozelot.jozelotProxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import de.jozelot.jozelotProxy.utils.PlayerSends;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NetworkCommand implements SimpleCommand {

    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final ConfigManager config;
    private final ProxyServer server;
    private MiniMessage mm = MiniMessage.miniMessage();

    public NetworkCommand(JozelotProxy plugin) {
        this.lang = plugin.getLang();
        this.plugin = plugin;
        this.consoleLogger = plugin.getConsoleLogger();
        this.config = plugin.getConfig();
        this.server = plugin.getServer();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("network.command")) {
            invocation.source().sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length > 0) {
            // RELOAD
            if (args[0].equalsIgnoreCase("reload") && source.hasPermission("network.command.reload")) {
                plugin.getPluginReload().reload();
                source.sendMessage(mm.deserialize(lang.getNetworkReloadSuccess()));

                String senderName = (source instanceof Player player)
                        ? player.getUsername()
                        : "Konsole";
                consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + senderName + "<" + config.getColorPrimary() + "> hat das Netzwerk neu geladen");
                for (Player player : server.getAllPlayers()) {
                    if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                        player.sendMessage(mm.deserialize(lang.format("network-reload-success-admin", Map.of("player-name", senderName))));
                    }
                }
                return;
            }

            // HELP
            if (args[0].equalsIgnoreCase("help")) {
                showHelp(source);
                return;
            }
            return;
        }
        showHelp(source);
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("network.command");
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        List<String> suggestions = new ArrayList<>();
        String[] args = invocation.arguments();

        if (!invocation.source().hasPermission("network.command")) {
            return List.of();
        }

        if (args.length <= 1) {
            String currentInput = (args.length == 1) ? args[0].toLowerCase() : "";

            List<String> subCommands = new ArrayList<>();
            if (invocation.source().hasPermission("network.command.reload")) subCommands.add("reload");
            subCommands.add("help");

            for (String s : subCommands) {
                if (s.startsWith(currentInput)) {
                    suggestions.add(s);
                }
            }
        }
        return suggestions;
    }

    private void showHelp(CommandSource source) {
        List<String> helpLines = lang.formatList("network-help", null);

        for (String line : helpLines) {
            source.sendMessage(mm.deserialize(line));
        }
    }
}
