package de.jozelot.jozelotProxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.lang.reflect.Array;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlaytimeCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public PlaytimeCommand(JozelotProxy plugin) {
        this.server = plugin.getServer();
        this.plugin = plugin;
        this.lang = plugin.getLang();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.playtime")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        String targetName = (args.length > 0) ? args[0] : (source instanceof Player p ? p.getUsername() : null);

        if (targetName == null) {
            source.sendMessage(mm.deserialize(lang.format("command-playtime-usage", null)));
            return;
        }

        java.util.UUID targetUUID = plugin.getMySQLManager().getUUIDByUsername(targetName);
        if (targetUUID == null) {
            targetUUID = server.getPlayer(targetName).map(Player::getUniqueId).orElse(null);
        }

        if (targetUUID == null) {
            source.sendMessage(mm.deserialize(lang.format("command-playtime-no-playtime", Map.of("player-name", targetName))));
            return;
        }

        long playtime = 0;
        long liveTime = 0;
        String typeLabel = "dem Netzwerk";
        Optional<Player> onlinePlayer = server.getPlayer(targetUUID);

        if (args.length < 2) {
            playtime = plugin.getMySQLManager().getTotalNetworkPlaytime(targetUUID);
            if (onlinePlayer.isPresent()) {
                liveTime = plugin.getPlaytimeListener().getCurrentSessionTime(targetUUID);
            }
        } else {
            String subTarget = args[1].toLowerCase();
            if (!invocation.source().hasPermission("network.command.playtime.admin")) {
                source.sendMessage(mm.deserialize(lang.format("command-playtime-usage", null)));
                return;
            }

            if (subTarget.startsWith("server:")) {
                String serverName = subTarget.replace("server:", "");
                playtime = plugin.getMySQLManager().getServerPlaytime(targetUUID, serverName);
                typeLabel = "dem Server " + serverName;

                if (onlinePlayer.isPresent()) {
                    String current = onlinePlayer.get().getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("");
                    if (current.equalsIgnoreCase(serverName)) {
                        liveTime = plugin.getPlaytimeListener().getCurrentSessionTime(targetUUID);
                    }
                }
            } else if (subTarget.startsWith("group:")) {
                String groupName = subTarget.replace("group:", "");
                playtime = plugin.getMySQLManager().getGroupPlaytime(targetUUID, groupName);
                typeLabel = "der Gruppe " + groupName;

                if (onlinePlayer.isPresent()) {
                    int currentGroupId = plugin.getGroupManager().getGroupId(onlinePlayer.get().getCurrentServer().map(s -> s.getServerInfo().getName()).orElse(""));
                    int targetGroupId = plugin.getMySQLManager().getGroupIdByIdentifier(groupName);
                    if (currentGroupId == targetGroupId && targetGroupId != -1) {
                        liveTime = plugin.getPlaytimeListener().getCurrentSessionTime(targetUUID);
                    }
                }
            } else {
                source.sendMessage(mm.deserialize(lang.format("command-playtime-syntax-error", null)));
                return;
            }
        }

        long totalPlaytime = playtime + liveTime;

        if (totalPlaytime == 0) {
            source.sendMessage(mm.deserialize(lang.format("command-playtime-no-playtime", Map.of("player-name", targetName))));
            return;
        }

        long[] time = formatTime(totalPlaytime);

        source.sendMessage(mm.deserialize(lang.format("command-playtime-total", Map.of(
                "player-name", targetName,
                "type", typeLabel,
                "days", String.valueOf(time[0]),
                "hours", String.valueOf(time[1]),
                "minutes", String.valueOf(time[2]),
                "seconds", String.valueOf(time[3])
        ))));
    }

    private long[] formatTime(long playtime) {
        Duration d = Duration.ofMillis(playtime);

        return new long[]{
                d.toDays(),
                (long) d.toHoursPart(),
                (long) d.toMinutesPart(),
                (long) d.toSecondsPart()
        };
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (!invocation.source().hasPermission("network.command.playtime")) { return List.of(); }

        if (args.length <= 1) {
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(s -> s.toLowerCase().startsWith(currentArg))
                    .toList();
        }

        if (!invocation.source().hasPermission("network.command.playtime.admin")) {return List.of();}

        if (args.length == 2) {
            if (!currentArg.contains(":")) {
                return List.of("server:", "group:").stream()
                        .filter(s -> s.startsWith(currentArg))
                        .toList();
            }

            if (currentArg.startsWith("server:")) {
                String subQuery = currentArg.replace("server:", "");
                return plugin.getMySQLManager().getRegisteredServerCache().stream()
                        .map(name -> "server:" + name)
                        .filter(s -> s.toLowerCase().startsWith(currentArg))
                        .toList();
            }

            if (currentArg.startsWith("group:")) {
                return plugin.getGroupManager().getAllGroupIdentifiers().stream()
                        .map(name -> "group:" + name)
                        .filter(s -> s.toLowerCase().startsWith(currentArg))
                        .toList();
            }
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.playtime");
    }

}
