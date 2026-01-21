package de.jozelot.jozelotProxy.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ServerSwitchListener {

    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ConfigManager config;
    private MiniMessage mm = MiniMessage.miniMessage();

    public ServerSwitchListener(JozelotProxy plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.config = plugin.getConfig();

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            for (Player player : plugin.getServer().getAllPlayers()) {
                player.getCurrentServer().ifPresent(conn -> {
                    int groupId = plugin.getGroupManager().getGroupId(conn.getServerInfo().getName());
                    if (groupId != -1) {
                        if (plugin.getGroupManager().isTabEnabled(groupId)) {
                            updateTabHeaderForPlayer(player, groupId);
                        }
                        updateTabEntryPings(player, groupId);
                    }
                });
            }
        }).repeat(java.time.Duration.ofSeconds(3)).schedule();
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        plugin.getBrandNameChanger().sendBrandName(player, config.getBrandName());
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String serverName = event.getServer().getServerInfo().getName();
        RegisteredServer connectedServer = event.getServer();

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            plugin.getMySQLManager().updatePlayerServer(player.getUniqueId(), connectedServer.getServerInfo().getName());
            plugin.getBrandNameChanger().sendBrandName(player, config.getBrandName());
            updateTabForGroup(player, connectedServer);
        }).delay(java.time.Duration.ofMillis(250)).schedule();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        removeFromAllTabs(player);
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        if (event.getPlayer().getCurrentServer().isPresent()) {
            removeFromAllTabs(event.getPlayer());
        }
    }

    private void removeFromAllTabs(Player playerToRemove) {
        for (Player all : plugin.getServer().getAllPlayers()) {
            all.getTabList().removeEntry(playerToRemove.getUniqueId());
        }
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
        UUID uuid = player.getUniqueId();

        Map<String, String> banInfo = plugin.getMySQLManager().getActiveBan(uuid);

        if (banInfo != null) {
            if (!player.hasPermission("network.ban.bypass")) {
                List<String> banLines = lang.formatList("ban-join-screen", Map.of(
                        "reason", banInfo.get("reason"),
                        "duration", banInfo.get("duration")
                ));

                event.setResult(ResultedEvent.ComponentResult.denied(
                        mm.deserialize(String.join("<newline>", banLines))
                ));
                return;
            }
        }

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

    public void updateTabForGroup(Player player, RegisteredServer connectedServer) {
        int groupId = plugin.getGroupManager().getGroupId(connectedServer.getServerInfo().getName());
        if (groupId == -1) return;

        List<Player> groupPlayers = plugin.getServer().getAllPlayers().stream()
                .filter(p -> p.getCurrentServer().isPresent())
                .filter(p -> plugin.getGroupManager().getGroupId(p.getCurrentServer().get().getServerInfo().getName()) == groupId)
                .collect(Collectors.toList());

        for (Player member : groupPlayers) {
            member.getTabList().clearAll();

            for (Player networkPlayer : groupPlayers) {
                String prefix = plugin.getLuckpermsUtils().getPlayerPrefix(networkPlayer);
                int weight = plugin.getLuckpermsUtils().getWeight(networkPlayer);

                int priority = weight;

                String displayNameRaw = lang.format("tab-player-format", Map.of(
                        "rank-prefix", prefix != null ? prefix : "",
                        "player-name", networkPlayer.getUsername()
                ));

                TabListEntry entry = TabListEntry.builder()
                        .profile(networkPlayer.getGameProfile())
                        .tabList(member.getTabList())
                        .displayName(mm.deserialize(displayNameRaw))
                        .latency((int) networkPlayer.getPing())
                        .listOrder(priority)
                        .build();

                member.getTabList().addEntry(entry);
            }
        }
    }

    private void updateTabEntryPings(Player viewer, int groupId) {
        for (TabListEntry entry : viewer.getTabList().getEntries()) {
            plugin.getServer().getPlayer(entry.getProfile().getId()).ifPresent(target -> {
                if (entry.getLatency() != (int) target.getPing()) {
                    entry.setLatency((int) target.getPing());
                }
            });
        }
    }

    public void updateTabHeaderForPlayer(Player p, int groupId) {
        String groupName = plugin.getGroupManager().getGroupName(groupId);
        List<String> serversInGroup = plugin.getGroupManager().getServersInGroup(groupId);

        String currentServerInternal = p.getCurrentServer()
                .map(conn -> conn.getServerInfo().getName())
                .orElse("Unbekannt");

        String currentServerDisplay = plugin.getMySQLManager().getServerDisplayName(currentServerInternal);
        String serverNameToShow = (currentServerDisplay != null) ? currentServerDisplay : currentServerInternal;

        String infoLine = (serversInGroup.size() <= 1)
                ? "(Lokal)"
                : groupName + " (Netzwerk)";

        Map<String, String> placeholders = Map.of(
                "group-info", infoLine,
                "group-name", groupName,
                "server-name", serverNameToShow,
                "ping", String.valueOf(p.getPing())
        );

        List<String> headerLines = lang.formatList("tab-header", placeholders);
        List<String> footerLines = lang.formatList("tab-footer", placeholders);

        p.sendPlayerListHeaderAndFooter(
                mm.deserialize(String.join("\n", headerLines)),
                mm.deserialize(String.join("\n", footerLines))
        );
    }
}
