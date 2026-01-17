package de.jozelot.jozelotProxy.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;

public class ServerSwitchListener {

    private final JozelotProxy plugin;
    private final LangManager lang;
    private MiniMessage mm = MiniMessage.miniMessage();

    public ServerSwitchListener(JozelotProxy plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String serverName = event.getServer().getServerInfo().getName();

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            plugin.getMySQLManager().updatePlayerServer(player.getUniqueId(), serverName);
        }).schedule();
    }

    @Subscribe
    public void onPreServerSwitch(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer targetServer = event.getOriginalServer();
        String serverName = targetServer.getServerInfo().getName();

        if (player.getCurrentServer().isEmpty()) {
            player.getVirtualHost().ifPresent(host -> {
                String domain = host.getHostName().toLowerCase();

                if (domain.contains(".")) {
                    if (!player.hasPermission("network.forcedhost.all") &&
                            !player.hasPermission("network.forcedhost." + serverName)) {

                        event.setResult(ServerPreConnectEvent.ServerResult.denied());

                        player.disconnect(mm.deserialize(lang.format("no-forcedhost-permission",
                                Map.of("server-name", serverName))));
                    }
                }
            });

            if (event.getResult().isAllowed() == false) return;
        }

        if (player.hasPermission("network.maintenance.bypass.all") ||
                player.hasPermission("network.maintenance.bypass." + serverName)) {
            return;
        }

        if (plugin.getMySQLManager().isServerInMaintenance(serverName)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());

            String displayName = plugin.getMySQLManager().getServerDisplayName(serverName);
            String finalName = (displayName != null && !displayName.isEmpty()) ? displayName : serverName;

            if (player.getCurrentServer().isEmpty()) {
                player.disconnect(mm.deserialize(lang.format("server-maintenance-kick",
                        Map.of("server-name", finalName))));
            } else {
                player.sendMessage(mm.deserialize(lang.format("server-maintenance-no-access",
                        Map.of("server-name", finalName))));
            }
        }
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("network.maintenance.bypass.all") || player.hasPermission("network.maintenance.bypass.proxy")) {
            return;
        }

        if (plugin.getMySQLManager().isServerInMaintenance("proxy")) {
            List<String> kickLines = lang.formatList("proxy-maintenance-kick", null);

            String kickMessage = String.join("<newline>", kickLines);

            event.setResult(ResultedEvent.ComponentResult.denied(
                    mm.deserialize(kickMessage)
            ));
        }
    }
}
