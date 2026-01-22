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
import java.util.concurrent.CompletableFuture;

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


    private String getDisplayName(String identifier) {
        String displayName = plugin.getMySQLManager().getServerDisplayName(identifier);
        return (displayName != null && !displayName.isEmpty()) ? displayName : identifier;
    }

    /**
     * Connect the player to another backend server
     * It's called simple because it doesn't send a success message so you only notice it when an error encounters
     * @param player Player obejct to send to backend
     * @param serverName Name of the backend server
     */
    public void connectPlayerSimple(Player player, String serverName) {
        Optional<RegisteredServer> targetServer = server.getServer(serverName);
        Optional<ServerConnection> currentConnection = player.getCurrentServer();

        String finalName = getDisplayName(serverName);

        if (currentConnection.isPresent() && currentConnection.get().getServerInfo().getName().equals(serverName)) {
            player.sendMessage(mm.deserialize(lang.format("already-on-server", Map.of("server-name", finalName))));
            return;
        }

        if (targetServer.isEmpty()) {
            player.sendMessage(mm.deserialize(lang.format("server-not-found", Map.of("server-name", finalName))));
            return;
        }

        player.createConnectionRequest(targetServer.get()).connect().thenAccept(result -> {
            if (!result.isSuccessful()) {
                player.sendMessage(mm.deserialize(lang.format("connection-failed",
                        Map.of("server-name", finalName))));
            }
        });
    }

    /**
     * Only for the tpo command
     */
    public CompletableFuture<Boolean> sendPlayerToPlayer(Player player, Player target) {
        Optional<ServerConnection> targetConn = target.getCurrentServer();

        if (targetConn.isEmpty()) {
            player.sendMessage(mm.deserialize(lang.format("player-not-on-server", Map.of("player-name", target.getUsername()))));
            return CompletableFuture.completedFuture(false);
        }

        RegisteredServer targetServer = targetConn.get().getServer();
        String identifier = targetServer.getServerInfo().getName();
        String finalName = getDisplayName(identifier);

        if (player.getCurrentServer().isPresent() &&
                player.getCurrentServer().get().getServer().equals(targetServer)) {
            player.sendMessage(mm.deserialize(lang.format("already-on-server", Map.of("server-name", finalName))));
            return CompletableFuture.completedFuture(false);
        }

        return player.createConnectionRequest(targetServer).connect().thenApply(result -> {
            if (result.isSuccessful()) {
                player.sendMessage(mm.deserialize(lang.format("send-to-server-success",
                        Map.of("player-name", target.getUsername(), "server-name", finalName))));
                return true;
            } else {
                player.sendMessage(mm.deserialize(lang.format("connection-failed", Map.of("server-name", finalName))));
                return false;
            }
        });
    }

    /**
     * Only for the tpohere command
     */
    public CompletableFuture<Boolean> sendPlayerToPlayer2(Player player, Player target, boolean silent) {
        Optional<ServerConnection> adminConn = player.getCurrentServer();

        if (adminConn.isEmpty()) {
            player.sendMessage(mm.deserialize(lang.format("generic-error", null)));
            return CompletableFuture.completedFuture(false);
        }

        RegisteredServer targetServer = adminConn.get().getServer();
        String identifier = targetServer.getServerInfo().getName();
        String finalName = getDisplayName(identifier);

        if (target.getCurrentServer().isPresent() &&
                target.getCurrentServer().get().getServer().equals(targetServer)) {
            player.sendMessage(mm.deserialize(lang.format("command-tpohere-already-on-server", Map.of("player-name", target.getUsername()))));
            return CompletableFuture.completedFuture(false);
        }

        return target.createConnectionRequest(targetServer).connect().thenApply(result -> {
            if (result.isSuccessful()) {
                player.sendMessage(mm.deserialize(lang.format("command-tpohere-success", Map.of("player-name", target.getUsername()))));

                if (!silent) {
                    target.sendMessage(mm.deserialize(lang.format("command-tpohere-target-info", Map.of("player-name", player.getUsername()))));
                }
                return true;
            } else {
                player.sendMessage(mm.deserialize(lang.format("connection-failed", Map.of("server-name", finalName))));
                return false;
            }
        });
    }
}