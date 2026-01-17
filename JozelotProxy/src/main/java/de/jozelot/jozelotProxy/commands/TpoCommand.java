package de.jozelot.jozelotProxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import de.jozelot.jozelotProxy.utils.PlayerSends;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TpoCommand implements SimpleCommand {

    private final LangManager lang;
    private final ProxyServer server;
    private final PlayerSends playerSends;
    private MiniMessage mm = MiniMessage.miniMessage();

    public TpoCommand(JozelotProxy plugin) {
        this.lang = plugin.getLang();
        this.server = plugin.getServer();
        this.playerSends = plugin.getPlayerSends();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.tpo")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        if (!(source instanceof Player player)) {
            source.sendMessage(mm.deserialize(lang.getOnlyPlayer()));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(mm.deserialize(lang.format("command-tpo-usage", null)));
            return;
        }

        Optional<Player> target = server.getPlayer(args[0]);

        if (!target.isPresent()) {
            player.sendMessage(mm.deserialize(lang.format("command-tpo-player-not-found", Map.of("player-name", args[0]))));
            return;
        }

        Player targetFinal = target.get();

        playerSends.sendPlayerToPlayer(player, targetFinal);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (!invocation.source().hasPermission("network.command.tpo")) return List.of();

        if (args.length <= 1) {
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .sorted()
                    .toList();
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.tpo");
    }
}
