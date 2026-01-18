package de.jozelot.jozelotProxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;

public class ServerCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ServerCommand(JozelotProxy plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.lang = plugin.getLang();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();


        if (!source.hasPermission("network.command.server")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        if (!(source instanceof Player player)) {
            source.sendMessage(mm.deserialize(lang.getOnlyPlayer()));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(mm.deserialize(lang.format("command-server-usage", null)));
            return;
        }

        String targetServerName = args[0];

        String displayName = plugin.getMySQLManager().getServerDisplayName(targetServerName);
        String finalName = (displayName != null && !displayName.isEmpty()) ? displayName : targetServerName;

        player.sendMessage(mm.deserialize(lang.format("command-server-try", Map.of("server-name", finalName))));

        plugin.getPlayerSends().connectPlayerSimple(player, targetServerName);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (args.length <= 1) {
            return server.getAllServers().stream()
                    .map(s -> s.getServerInfo().getName())
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .sorted()
                    .toList();
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.server");
    }
}