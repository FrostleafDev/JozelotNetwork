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
                UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
                plugin.getMySQLManager().logAction(operatorUUID, "RELOAD", "server:proxy", "Kompletter Reload des Netzwerks");
                return;
            }

            // HELP
            if (args[0].equalsIgnoreCase("help")) {
                showHelp(source);
                return;
            }

            // START
            if (args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("stop")) {
                if (args.length < 2) { showHelp(source); return; }

                String action = args[0].toLowerCase();
                String serverName = args[1];

                String pteroId = plugin.getMySQLManager().getPteroIdentifier(serverName);

                if (pteroId == null) {
                    source.sendMessage(mm.deserialize(lang.format("pterodactyl-control-no-id", Map.of("server-name", serverName))));
                    return;
                }

                if ((action.equalsIgnoreCase("restart") || action.equalsIgnoreCase("stop")) && serverName.equalsIgnoreCase("proxy")) {

                    List<String> kickLines = lang.formatList("pterodactyl-restart-kick", null);

                    server.getAllPlayers().forEach(p -> p.disconnect(mm.deserialize(String.join("<newline>", kickLines))));
                }

                plugin.getPteroManager().sendAction(pteroId, action, code -> {
                    if (code == 204) {
                        String senderName = (source instanceof Player player) ? player.getUsername() : "Konsole";
                        source.sendMessage(mm.deserialize(lang.format("pterodactyl-control-success", Map.of("action", action, "server-name", serverName))));

                        consoleLogger.broadCastToConsole("<yellow>" + senderName + " <gray>hat <white>" + serverName + " <gray>den Befehl zu <gold>" + action + " <gray>gesendet");
                        for (Player player : server.getAllPlayers()) {
                            if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                                player.sendMessage(mm.deserialize(lang.format("pterodactyl-control-success-admin", Map.of("player-name", senderName, "server-name", serverName, "action", action))));
                            }
                        }

                        UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
                        plugin.getMySQLManager().logAction(operatorUUID, "SERVER_ACTION", "server:" + serverName, "Action: " + action);

                    } else {
                        source.sendMessage(mm.deserialize(lang.format("pterodactyl-control-api-error", Map.of("code", String.valueOf(code)))));
                    }
                });
                return;
            }

            if (args[0].equalsIgnoreCase("status")) {
                if (args.length < 2) { showHelp(source); return; }
                String serverName = args[1];
                String pteroId = plugin.getMySQLManager().getPteroIdentifier(serverName);

                if (pteroId == null) {
                    source.sendMessage(mm.deserialize(lang.format("pterodactyl-control-no-id", Map.of("server-name", serverName))));
                    return;
                }

                plugin.getPteroManager().getResources(pteroId, data -> {
                    if (data == null) {
                        source.sendMessage(mm.deserialize("<red>Fehler beim Abrufen der API-Daten."));
                        return;
                    }

                    String state = data.get("current_state").getAsString(); // running, starting, offline
                    long memory = data.getAsJsonObject("resources").get("memory_bytes").getAsLong();
                    double cpu = data.getAsJsonObject("resources").get("cpu_absolute").getAsDouble();
                    long networkIn = data.getAsJsonObject("resources").get("network_rx_bytes").getAsLong();
                    long networkOut = data.getAsJsonObject("resources").get("network_tx_bytes").getAsLong();

                    String stateColor = state.equals("running") ? "<#00FC00>" : (state.equals("starting") ? "<#FCE300>" : "<#f90036>");

                    // Nachricht bauen
                    List<String> infoLines = lang.formatList("pterodactyl-status-message", Map.of(
                            "server-name", serverName,
                            "id", pteroId.substring(0, 8),
                            "status", stateColor + state.toUpperCase(),
                            "cpu", String.format("%.2f", cpu),
                            "memory", String.valueOf((memory / 1024 / 1024)),
                            "network-in", String.valueOf((networkIn / 1024)),
                            "network-out", String.valueOf((networkOut / 1024))
                    ));
                    source.sendMessage(mm.deserialize(String.join("<newline>", infoLines)));

                });
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

                String type = args[1].toLowerCase();
                String serverName = args[2];
                String action = args[3].toLowerCase();

                if (!plugin.getMySQLManager().existsInDatabase(serverName)) {
                    source.sendMessage(mm.deserialize(lang.format("server-not-found", Map.of("server-name", serverName))));
                    return;
                }

                plugin.getServer().getScheduler().buildTask(plugin, () -> {
                    String senderName = (source instanceof Player player) ? player.getUsername() : "Konsole";

                    // --- DISPLAY NAME ---
                    if (type.equals("display_name")) {
                        if (action.equals("get")) {
                            String displayName = plugin.getMySQLManager().getServerDisplayName(serverName);
                            source.sendMessage(mm.deserialize(lang.format("command-manage-display-name-get", Map.of("server-name", serverName, "display-name", displayName))));
                        } else if (action.equals("set")) {
                            StringJoiner joiner = new StringJoiner(" ");
                            for (int i = 4; i < args.length; i++) joiner.add(args[i]);
                            String fullDisplayName = joiner.toString();

                            plugin.getMySQLManager().setServerDisplayName(serverName, fullDisplayName);
                            source.sendMessage(mm.deserialize(lang.format("command-manage-display-name-success", Map.of("server-name", serverName, "display-name", fullDisplayName))));

                            consoleLogger.broadCastToConsole("<yellow>" + senderName + " <gray>hat <white>" + serverName + " <gray>in <gold>" + fullDisplayName + " <gray>umbenannt");
                            for (Player player : server.getAllPlayers()) {
                                if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                                    player.sendMessage(mm.deserialize(lang.format("command-manage-display-name-success-admin", Map.of("player-name", senderName, "server-name", serverName, "display-name", fullDisplayName))));
                                }
                            }

                            UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
                            plugin.getMySQLManager().logAction(operatorUUID, "DISPLAY_NAME_CHANGE", "server:" + serverName, "Zu: " + fullDisplayName);
                        }
                    }

                    // --- MAX PLAYERS ---
                    else if (type.equals("max_players")) {
                        if (action.equals("get")) {
                            int maxPlayers = plugin.getMySQLManager().getServerMaxPlayers(serverName);
                            source.sendMessage(mm.deserialize(lang.format("command-manage-max-players-get", Map.of("server-name", serverName, "max-players", String.valueOf(maxPlayers)))));
                        } else if (action.equals("set")) {
                            try {
                                int amount = Integer.parseInt(args[4]);
                                plugin.getMySQLManager().setServerMaxPlayers(serverName, amount);
                                source.sendMessage(mm.deserialize(lang.format("command-manage-max-players-success", Map.of("server-name", serverName, "max-players", String.valueOf(amount)))));

                                consoleLogger.broadCastToConsole("<yellow>" + senderName + " <gray>hat die Slots von <white>" + serverName + " <gray>auf <gold>" + amount + " <gray>gesetzt");
                                for (Player player : server.getAllPlayers()) {
                                    if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                                        player.sendMessage(mm.deserialize(lang.format("command-manage-max-players-success-admin", Map.of("player-name", senderName, "server-name", serverName, "max-players", String.valueOf(amount)))));
                                    }
                                }
                                UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
                                plugin.getMySQLManager().logAction(operatorUUID, "MAX_PLAYERS_CHANGE", "server:" + serverName, "Zu: " + amount);
                            } catch (NumberFormatException e) {
                                source.sendMessage(mm.deserialize(lang.format("command-manage-max-players-not-a-number", Map.of("input", args[4]))));
                            }
                        }
                    }

                    // --- MOTD ---
                    else if (type.equals("motd")) {
                        if (action.equals("get")) {
                            String motd = plugin.getMySQLManager().getServerMOTD(serverName);
                            source.sendMessage(mm.deserialize(lang.format("command-manage-motd-get", Map.of("server-name", serverName, "motd", motd))));
                        } else if (action.equals("set")) {
                            StringJoiner joiner = new StringJoiner(" ");
                            for (int i = 4; i < args.length; i++) joiner.add(args[i]);
                            String fullMotd = joiner.toString();

                            plugin.getMySQLManager().setServerMOTD(serverName, fullMotd);
                            source.sendMessage(mm.deserialize(lang.format("command-manage-motd-success", Map.of("server-name", serverName, "motd", fullMotd))));

                            consoleLogger.broadCastToConsole("<yellow>" + senderName + " <gray>hat die MOTD von <white>" + serverName + " <gray>geändert");
                            for (Player player : server.getAllPlayers()) {
                                if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                                    player.sendMessage(mm.deserialize(lang.format("command-manage-motd-success-admin", Map.of("player-name", senderName, "server-name", serverName, "motd", fullMotd))));
                                }
                            }
                            UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
                            plugin.getMySQLManager().logAction(operatorUUID, "MOTD_CHANGE", "server:" + serverName, "Zu: " + fullMotd);
                        }
                    }

                    // --- MAINTENANCE ---
                    else if (type.equals("maintenance")) {
                        if (action.equals("get")) {
                            boolean state = plugin.getMySQLManager().getServerMaintenance(serverName);
                            source.sendMessage(mm.deserialize(lang.format("command-manage-maintenance-get", Map.of("server-name", serverName, "status", state ? "An" : "Aus"))));
                        } else if (action.equals("set")) {
                            boolean newState = args[4].equalsIgnoreCase("true") || args[4].equalsIgnoreCase("on");
                            plugin.getMySQLManager().setServerMaintenance(serverName, newState);

                            String statusText = newState ? "aktiviert" : "deaktiviert";
                            source.sendMessage(mm.deserialize(lang.format("command-manage-maintenance-success", Map.of("server-name", serverName, "status", statusText))));

                            consoleLogger.broadCastToConsole("<yellow>" + senderName + " <gray>hat Wartungen für <white>" + serverName + " <gold>" + statusText);
                            for (Player player : server.getAllPlayers()) {
                                if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                                    player.sendMessage(mm.deserialize(lang.format("command-manage-maintenance-success-admin", Map.of("player-name", senderName, "server-name", serverName, "status", statusText))));
                                }
                            }
                            UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
                            plugin.getMySQLManager().logAction(operatorUUID, "MAINTENANCE_CHANGE", "server:" + serverName, "Status: " + statusText);
                        }
                    }
                }).schedule();
                return;
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
                if (has(invocation, "manage.server.start")) list.add("start");
                if (has(invocation, "manage.server.stop")) list.add("stop");
                if (has(invocation, "manage.server.restart")) list.add("restart");
                if (has(invocation, "manage.server.status")) list.add("status");

                list.add("help");
                break;

            case 2:
                String cmd = args[0].toLowerCase();
                if (List.of("start", "stop", "restart", "status").contains(cmd)) {
                    list.addAll(plugin.getMySQLManager().getRegisteredServerCache());
                } else if (args[0].equalsIgnoreCase("manage") && has(invocation, "manage")) {
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
