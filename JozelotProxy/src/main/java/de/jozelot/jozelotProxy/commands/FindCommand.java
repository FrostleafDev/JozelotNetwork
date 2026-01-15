package de.jozelot.jozelotProxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FindCommand implements SimpleCommand {

    private final LangManager lang;
    private final ProxyServer server;
    MiniMessage mm = MiniMessage.miniMessage();
    private final JozelotProxy plugin;

    public FindCommand(JozelotProxy plugin) {
        this.lang = plugin.getLang();
        this.server = plugin.getServer();
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.find")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }
        if (args.length == 0) {
            source.sendMessage(mm.deserialize(lang.format("command-find-missing-argument", null)));
            return;
        }

        String rawTarget = args[0];
        boolean forceOnline = rawTarget.startsWith("online:");
        boolean forceOffline = rawTarget.startsWith("offline:");

        final String targetName = rawTarget.replace("online:", "").replace("offline:", "");
        Optional<Player> onlineTarget = server.getPlayer(targetName);

        if (forceOnline && onlineTarget.isEmpty()) {
            source.sendMessage(mm.deserialize(lang.format("command-find-use-offline", Map.of("player-name", targetName))));
            return;
        }

        if (forceOffline && onlineTarget.isPresent()) {
            source.sendMessage(mm.deserialize(lang.format("command-find-use-online", Map.of("player-name", targetName))));
            return;
        }

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            Map<String, String> data = plugin.getMySQLManager().getOfflinePlayerInfo(targetName);

            if (data == null) {
                source.sendMessage(mm.deserialize(lang.format("command-find-not-found", Map.of("player-name", targetName))));
                return;
            }

            String playerName = data.get("name");
            String serverDisplayName = data.get("server");

            if (onlineTarget.isPresent()) {
                onlineTarget.get().getCurrentServer().ifPresentOrElse(connection -> {
                    source.sendMessage(mm.deserialize(lang.format("command-find-success",
                            Map.of("player-name", playerName, "server-name", serverDisplayName))));
                }, () -> source.sendMessage(mm.deserialize(lang.format("command-find-connecting", Map.of("player-name", playerName)))));
            } else {
                source.sendMessage(mm.deserialize(lang.format("command-find-offline",
                        Map.of("player-name", playerName, "server-name", serverDisplayName))));
            }
        }).schedule();
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length > 1) return List.of();

        String input = args.length == 1 ? args[0].toLowerCase() : "";
        List<String> suggestions = new ArrayList<>();

        if (input.startsWith("offline:")) {
            String search = input.substring(8);
            if (search.length() >= 1) {
                suggestions.addAll(plugin.getMySQLManager().getSimilarOfflinePlayers(search)
                        .stream()
                        .map(name -> "offline:" + name)
                        .toList());
            }
        }
        else if (input.startsWith("online:")) {
            String search = input.substring(7);
            server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(search))
                    .forEach(name -> suggestions.add("online:" + name));
        }
        else {
            if ("online:".startsWith(input)) suggestions.add("online:");
            if ("offline:".startsWith(input)) suggestions.add("offline:");

            server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .forEach(suggestions::add);
        }

        return suggestions;
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("network.command.find");
    }
}
