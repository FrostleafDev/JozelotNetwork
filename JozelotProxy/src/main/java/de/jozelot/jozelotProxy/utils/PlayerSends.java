package de.jozelot.jozelotProxy.utils;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;
import java.util.Optional;

public class PlayerSends {

    private final JozelotProxy plugin;
    private final ProxyServer server;
    private final ConfigManager config;
    private final LangManager lang;
    MiniMessage mm = MiniMessage.miniMessage();

    public PlayerSends(JozelotProxy plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.config = plugin.getConfig();
        this.lang = plugin.getLang();
    }

    public void connectPlayerSimple(Player player, String serverName) {
        Optional<RegisteredServer> targetServer = server.getServer(serverName);
        Optional<ServerConnection> currentConnection = player.getCurrentServer();

        if (currentConnection.isPresent() && currentConnection.get().getServerInfo().getName().equals(serverName)) {
            player.sendMessage(mm.deserialize(lang.format("already-on-server", Map.of("server-name", serverName))));
            return;
        }

        if (targetServer.isEmpty()) {
            player.sendMessage(mm.deserialize(lang.format("server-not-found", Map.of("server-name", serverName))));
            return;
        }

        plugin.getServer().getScheduler().buildTask(plugin, () -> {

            String displayName = plugin.getMySQLManager().getServerDisplayName(serverName);

            String finalName = (displayName != null && !displayName.isEmpty()) ? displayName : serverName;

            player.createConnectionRequest(targetServer.get()).connect().thenAccept(result -> {
                if (!result.isSuccessful()) {
                    player.sendMessage(mm.deserialize(lang.format("connection-failed",
                            Map.of("server-name", finalName))));
                }
            });
        }).schedule();
    }

    public void sendPlayerToPlayer(Player player, Player target) {
        Optional<ServerConnection> targetConn = target.getCurrentServer();

        if (targetConn.isEmpty()) {
            player.sendMessage(mm.deserialize("<red>Spieler ist auf keinem Server!"));
            return;
        }

        RegisteredServer targetServer = targetConn.get().getServer();
        String serverName = targetServer.getServerInfo().getName();

        if (player.getCurrentServer().isPresent() &&
                player.getCurrentServer().get().getServer().equals(targetServer)) {
            player.sendMessage(mm.deserialize(lang.format("already-on-server", Map.of("server-name", serverName))));
            return;
        }

        player.createConnectionRequest(targetServer).connect().thenAccept(result -> {
            if (result.isSuccessful()) {
            } else {
                player.sendMessage(mm.deserialize(lang.format("connection-failed", Map.of("server-name", serverName))));
            }
        });
    }
}
