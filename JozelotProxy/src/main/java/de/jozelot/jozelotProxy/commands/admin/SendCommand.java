package de.jozelot.jozelotProxy.commands.admin;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;
import java.util.stream.Collectors;

public class SendCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SendCommand(JozelotProxy plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.lang = plugin.getLang();
        this.consoleLogger = plugin.getConsoleLogger();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.send")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        if (args.length < 2) {
            source.sendMessage(mm.deserialize(lang.format("command-send-usage", null)));
            return;
        }

        String targetInput = args[0];
        String destinationIdentifier = args[1];

        Optional<RegisteredServer> destServer = server.getServer(destinationIdentifier);
        String destinationDisplayName = plugin.getMySQLManager().getServerDisplayName(destinationIdentifier);

        if (destServer.isEmpty()) {
            source.sendMessage(mm.deserialize(lang.format("server-not-found", Map.of("server-name", destinationDisplayName))));
            return;
        }

        List<Player> playersToSend = new ArrayList<>();

        if (targetInput.startsWith("server:")) {
            String type = targetInput.substring(7).toLowerCase();

            switch (type) {
                case "all" -> playersToSend.addAll(server.getAllPlayers());
                case "current" -> {
                    if (source instanceof Player p && p.getCurrentServer().isPresent()) {
                        playersToSend.addAll(p.getCurrentServer().get().getServer().getPlayersConnected());
                    } else {
                        source.sendMessage(mm.deserialize(lang.format("only-player", null)));
                        return;
                    }
                }
                default -> {
                    server.getServer(type).ifPresentOrElse(
                            s -> playersToSend.addAll(s.getPlayersConnected()),
                            () -> {
                                String sourceDisplay = plugin.getMySQLManager().getServerDisplayName(type);
                                source.sendMessage(mm.deserialize(lang.format("server-not-found", Map.of("server-name", sourceDisplay))));
                            }
                    );
                }
            }
        } else {
            server.getPlayer(targetInput).ifPresentOrElse(
                    playersToSend::add,
                    () -> source.sendMessage(mm.deserialize(lang.format("command-find-not-found", Map.of("player-name", targetInput))))
            );
        }

        if (playersToSend.isEmpty()) return;

        for (Player p : playersToSend) {
            plugin.getPlayerSends().connectPlayerSimple(p, destinationIdentifier);
        }

        source.sendMessage(mm.deserialize(lang.format("command-send-success-multiple",
                Map.of("count", String.valueOf(playersToSend.size()), "server-name", destinationDisplayName))));

        String senderName = (source instanceof Player player) ? player.getUsername() : "Konsole";

        consoleLogger.broadCastToConsole("<yellow>" + senderName + " <gray>hat <white>" + args[0] + " <gray>auf <gold>" + args[1] + " <gray>verschoben");
        for (Player player : server.getAllPlayers()) {
            if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                player.sendMessage(mm.deserialize(lang.format("command-send-success-admin", Map.of("player-name", senderName, "server-name", args[1], "range", args[0]))));
            }
        }

        UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
        plugin.getMySQLManager().logAction(operatorUUID, "SEND", args[0], "Ziel: " + args[1]);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (args.length <= 1) {
            List<String> suggestions = new ArrayList<>();

            if (!currentArg.startsWith("server:")) {
                server.getAllPlayers().forEach(p -> suggestions.add(p.getUsername()));
                suggestions.add("server:");
            } else {
                suggestions.add("server:all");
                suggestions.add("server:current");
                server.getAllServers().forEach(s -> suggestions.add("server:" + s.getServerInfo().getName()));
            }

            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(currentArg))
                    .sorted()
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return server.getAllServers().stream()
                    .map(s -> s.getServerInfo().getName())
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.send");
    }
}