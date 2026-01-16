package de.jozelot.jozelotProxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import de.jozelot.jozelotProxy.utils.PlayerSends;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;

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

                // LOGS
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
                // LOGS
                return;
            }

            // HELP
            if (args[0].equalsIgnoreCase("help")) {
                showHelp(source);
                return;
            }

            // MANAGE
            if (args[0].equalsIgnoreCase("manage") && source.hasPermission("network.command.manage")) {
                if (args.length < 4) {
                    showHelp(source);
                    return;
                }

                if (args[3].equalsIgnoreCase("set") && args.length < 5) {
                    showHelp(source);
                    return;
                }

                if (args[1].equalsIgnoreCase("display_name")) {
                    String serverName = args[2];

                    if (!plugin.getMySQLManager().existsInDatabase(serverName)) {
                        source.sendMessage(mm.deserialize("Server " + serverName + " existiert nicht"));
                        return;
                    }
                    plugin.getServer().getScheduler().buildTask(plugin, () -> {
                        if (args[3].equalsIgnoreCase("get")) {
                           String displayName = plugin.getMySQLManager().getServerDisplayName(serverName);

                           source.sendMessage(mm.deserialize(lang.format("command-manage-display-name-get", Map.of("server-name", serverName, "display-name", displayName))));

                           return;
                        }
                        if (args[3].equalsIgnoreCase("set")) {
                            StringJoiner joiner = new StringJoiner(" ");
                            for (int i = 4; i < args.length; i++) {
                                joiner.add(args[i]);
                            }
                            String fullDisplayName = joiner.toString();

                            plugin.getMySQLManager().setServerDisplayName(serverName, fullDisplayName);

                            // LOGS
                            source.sendMessage(mm.deserialize(lang.format("command-manage-display-name-success", Map.of("server-name", serverName, "display-name", fullDisplayName))));

                            String senderName = (source instanceof Player player)
                                    ? player.getUsername()
                                    : "Konsole";
                            consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + senderName + "<" + config.getColorPrimary() + "> hat " + serverName + " in " + fullDisplayName + " umbenannt");
                            for (Player player : server.getAllPlayers()) {
                                if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                                    source.sendMessage(mm.deserialize(lang.format("command-manage-display-name-success", Map.of("server-name", serverName, "display-name", fullDisplayName))));
                                }
                            }
                            // LOGS
                        }
                    }).schedule();
                    return;
                }
                if (args[1].equalsignorecase("motd")
            }
        }
        showHelp(source);
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("network.command");
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> list = new ArrayList<>();

        if (!invocation.source().hasPermission("network.command")) return List.of();

        int argCount = args.length;
        if (argCount == 0) {
            argCount = 1;
        }

        switch (argCount) {
            case 1:
                if (has(invocation, "reload")) list.add("reload");
                if (has(invocation, "manage")) list.add("manage");
                list.add("help");
                break;

            case 2:
                if (args[0].equalsIgnoreCase("manage") && has(invocation, "manage")) {
                    list.addAll(List.of("display_name", "max_players", "motd", "maintenance"));
                }
                break;

            case 3:
                if (args[0].equalsIgnoreCase("manage") && has(invocation, "manage")) {
                    list.addAll(plugin.getMySQLManager().getRegisteredServerCache());
                }
                break;

            case 4:
                if (args[0].equalsIgnoreCase("manage") && has(invocation, "manage")) {
                    list.addAll(List.of("get", "set"));
                }
                break;

            case 5:
                if (args[0].equalsIgnoreCase("manage") && has(invocation, "manage") && args[3].equalsIgnoreCase("set")) {
                    if (args[1].equalsIgnoreCase("max_players")) list.addAll(List.of("10", "20", "50", "100", "200"));
                    else if (args[1].equalsIgnoreCase("maintenance")) list.addAll(List.of("true", "false"));
                    else list.add("<wert>");
                }
                break;
        }

        String lastArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(lastArg))
                .sorted()
                .toList();
    }

    private boolean has(Invocation inv, String sub) {
        return inv.source().hasPermission("network.command." + sub);
    }

    private void showHelp(CommandSource source) {
        List<String> helpLines = lang.formatList("network-help", null);

        for (String line : helpLines) {
            source.sendMessage(mm.deserialize(line));
        }
    }
}
