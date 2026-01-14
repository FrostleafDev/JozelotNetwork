package de.jozelot.jozelotProxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.PlayerSends;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;
import java.util.Optional;

public class LobbyCommand implements SimpleCommand {

    MiniMessage mm = MiniMessage.miniMessage();
    private final ConfigManager config;
    private final JozelotProxy plugin;
    private final PlayerSends playerSends;
    private final LangManager lang;
    private final ProxyServer server;

    public LobbyCommand(JozelotProxy plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.playerSends = plugin.getPlayerSends();
        this.lang = plugin.getLang();
        this.server = plugin.getServer();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("network.command.lobby")) {
            invocation.source().sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(mm.deserialize(lang.getOnlyPlayer()));
            return;
        }
        Optional<RegisteredServer> targetServer = server.getServer(config.getLobbyServer());
        Optional<ServerConnection> currentConnection = player.getCurrentServer();

        if (currentConnection.get().getServer().getServerInfo().getName().equals(config.getLobbyServer())) {
            player.sendMessage(mm.deserialize(lang.format("already-on-lobby", null)));
            return;
        }
        player.sendMessage(mm.deserialize(lang.format("send-to-lobby", null)));
        playerSends.connectPlayerSimple(player, config.getLobbyServer());
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("network.command.lobby");
    }
}
