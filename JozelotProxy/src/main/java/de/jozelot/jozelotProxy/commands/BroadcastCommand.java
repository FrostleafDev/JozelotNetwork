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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BroadcastCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final ConfigManager config;
    private final ConsoleLogger consoleLogger;

    public BroadcastCommand(JozelotProxy plugin) {
        this.server = plugin.getServer();
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.config = plugin.getConfig();
        this.consoleLogger = plugin.getConsoleLogger();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.broadcast")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        if (args.length < 3) {
            source.sendMessage(mm.deserialize(lang.format("command-broadcast-usage", null)));
            return;
        }

        String area = args[0].toLowerCase();
        String type = args[1].toLowerCase();

        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String message = sb.toString().trim();

        final Collection<Player> targets = new ArrayList<>();

        if (area.equals("all")) {
            targets.addAll(server.getAllPlayers());
        } else if (area.equals("current_server") && source instanceof Player p) {
            p.getCurrentServer().ifPresent(s -> targets.addAll(s.getServer().getPlayersConnected()));
        } else if (area.equals("current_group") && source instanceof Player p) {
            p.getCurrentServer().ifPresent(s -> {
                int groupId = plugin.getGroupManager().getGroupId(s.getServerInfo().getName());
                targets.addAll(getPlayersInGroup(groupId));
            });
        } else if (area.startsWith("server:")) {
            String serverName = area.replace("server:", "");
            server.getServer(serverName).ifPresent(s -> targets.addAll(s.getPlayersConnected()));
        } else if (area.startsWith("group:")) {
            String groupName = area.replace("group:", "");
            int groupId = plugin.getMySQLManager().getGroupIdByIdentifier(groupName);
            targets.addAll(getPlayersInGroup(groupId));
        }

        if (targets.isEmpty()) {
            source.sendMessage(mm.deserialize(lang.format("command-broadcast-invalid", null)));
            return;
        }

        List<String> broadcastLines = lang.formatList("broadcast-" + type, Map.of("message", message));
        for (Player target : targets) {
            for (String line : broadcastLines) {
                target.sendMessage(mm.deserialize(line));
            }
        }

        source.sendMessage(mm.deserialize(lang.format("command-broadcast-send", Map.of("size", String.valueOf(targets.size())))));
        String name = (source instanceof Player player) ? player.getUsername() : "Konsole";

        consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + name + "<" + config.getColorPrimary() + "> hat einen Broadcast für " + targets.size() + " gesendet.");
        for (Player player : server.getAllPlayers()) {
            if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                player.sendMessage(mm.deserialize(lang.format("command-broadcast-send-admin", Map.of("player-name", name, "size", String.valueOf(targets.size())))));
            }
        }

    }

    private List<Player> getPlayersInGroup(int groupId) {
        return server.getAllPlayers().stream()
                .filter(p -> p.getCurrentServer().isPresent())
                .filter(p -> plugin.getGroupManager().getGroupId(p.getCurrentServer().get().getServerInfo().getName()) == groupId)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (!invocation.source().hasPermission("network.command.broadcast")) return List.of();

        if (args.length <= 1) {
            List<String> options = new ArrayList<>(List.of("all", "current_server", "current_group", "server:", "group:"));

            if (currentArg.startsWith("server:")) {
                return plugin.getMySQLManager().getRegisteredServerCache().stream()
                        .map(s -> "server:" + s).filter(s -> s.startsWith(currentArg)).toList();
            }
            if (currentArg.startsWith("group:")) {
                return plugin.getGroupManager().getAllGroupIdentifiers().stream()
                        .map(g -> "group:" + g).filter(g -> g.startsWith(currentArg)).toList();
            }
            return options.stream().filter(o -> o.startsWith(currentArg)).toList();
        }

        if (args.length == 2) {
            return List.of("announcement", "message", "information", "warning").stream()
                    .filter(t -> t.startsWith(currentArg)).toList();
        }

        return List.of();
    }
}