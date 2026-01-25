package de.jozelot.jozelotProxy.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Diese Klasse steuert den gesamten Lebenszyklus eines Spielers auf dem Proxy.
 * Vom ersten Verbindungsversuch über Serverwechsel bis zum Logout.
 */
public class ServerSwitchListener {

    private final JozelotProxy plugin;
    private final ProxyServer server;
    private final LangManager lang;
    private final ConfigManager config;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ServerSwitchListener(JozelotProxy plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.lang = plugin.getLang();
        this.config = plugin.getConfig();

        // Falls sich Ränge in LuckPerms ändern, updaten wir die Tablist sofort
        if (plugin.getLuckPerms() != null) {
            plugin.getLuckPerms().getEventBus().subscribe(plugin, net.luckperms.api.event.user.UserDataRecalculateEvent.class, event -> {
                server.getPlayer(event.getUser().getUniqueId()).ifPresent(player -> {
                    player.getCurrentServer().ifPresent(conn -> {
                        server.getScheduler().buildTask(plugin, () -> updateTabForGroup(player, conn.getServer())).schedule();
                    });
                });
            });
        }

        // Ein "Herzschlag" für das Netzwerk: Aktualisiert alle 3 Sek. Header, Footer und Pings
        server.getScheduler().buildTask(plugin, () -> {
            for (Player player : server.getAllPlayers()) {
                player.getCurrentServer().ifPresent(conn -> {
                    int groupId = plugin.getGroupManager().getGroupId(conn.getServerInfo().getName());
                    if (groupId != -1) {
                        if (plugin.getGroupManager().isTabEnabled(groupId)) updateTabHeaderForPlayer(player, groupId);
                        updateTabEntryPings(player);
                    }
                });
            }
        }).repeat(Duration.ofSeconds(3)).schedule();
    }

    // ==================================================================================
    // 1. LOGIN-PHASE (VOR DEM BEITRITT)
    // ==================================================================================

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 1. Ban-Check: Wer gebannt ist, kommt nicht rein (außer er hat die Bypass-Permission)
        Map<String, String> banInfo = plugin.getMySQLManager().getActiveBan(uuid);
        if (banInfo != null && !player.hasPermission("network.ban.bypass")) {
            event.setResult(ResultedEvent.ComponentResult.denied(mm.deserialize(
                    String.join("<newline>", lang.formatList("ban-join-screen", banInfo)))));
            return;
        }

        // 2. Netzwerk-Wartung: Ist der Proxy im Wartungsmodus?
        if (plugin.getMySQLManager().isServerInMaintenance("proxy") &&
                !player.hasPermission("network.maintenance.bypass.proxy")) {
            event.setResult(ResultedEvent.ComponentResult.denied(mm.deserialize(
                    String.join("<newline>", lang.formatList("proxy-maintenance-kick", null)))));
            return;
        }

        // 3. Version check
        int protocolMin = config.getProtocalMin();
        String versionMin = ProtocolVersion.getProtocolVersion(protocolMin).getName();

        int protocolMax = config.getProtocalMax();
        String versionMax = ProtocolVersion.getProtocolVersion(protocolMax).getName();

        int protocolVersion = player.getProtocolVersion().getProtocol();
        String versionPlayer = ProtocolVersion.getProtocolVersion(protocolVersion).getName();

        if (protocolVersion < protocolMin) {
            event.setResult(ResultedEvent.ComponentResult.denied(mm.deserialize(
                    String.join("<newline>", lang.formatList("protocol-to-old", Map.of("min-version", versionMin, "current-version", versionPlayer)))
            )));
            return;
        }
        if (protocolVersion > protocolMax) {
            event.setResult(ResultedEvent.ComponentResult.denied(mm.deserialize(
                    String.join("<newline>", lang.formatList("protocol-to-new", Map.of("ax-version", versionMax, "current-version", versionPlayer)))
            )));
            return;
        }

        // 4. Proxy-Kapazität: Ist das gesamte Netzwerk voll?
        int maxProxy = plugin.getMySQLManager().getMaxPlayers("proxy");
        if (maxProxy > 0 && server.getPlayerCount() >= maxProxy && !player.hasPermission("network.maxplayers.bypass.proxy")) {
            event.setResult(ResultedEvent.ComponentResult.denied(mm.deserialize(
                    String.join("<newline>", lang.formatList("proxy-full-kick", Map.of("max", String.valueOf(maxProxy)))))));
        }
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String username = player.getUsername();

        // Brand Name senden
        server.getScheduler().buildTask(plugin, () -> {
            plugin.getBrandNameChanger().sendBrandName(player, config.getBrandName());
        }).delay(250, TimeUnit.MILLISECONDS).schedule();

        int protocolReco = config.getProtocalReco();
        String versionReco = ProtocolVersion.getProtocolVersion(protocolReco).getName();
        int protocolVersion = player.getProtocolVersion().getProtocol();
        String versionPlayer = ProtocolVersion.getProtocolVersion(protocolVersion).getName();
        int protocolMax = config.getProtocalMax();
        String versionMax = ProtocolVersion.getProtocolVersion(protocolMax).getName();

        if (protocolVersion < protocolReco) {
            player.sendMessage(mm.deserialize(String.join("<newline>", lang.formatList("protocol-recomment", Map.of("current-version", versionPlayer, "reco-version", versionReco, "max-version", versionMax)))));
        }

        // Datenbank-Aufgaben asynchron erledigen
        server.getScheduler().buildTask(plugin, () -> {
            // Spieler in die Liste eintragen und prüfen, ob er neu ist
            boolean isNew = plugin.getMySQLManager().addToPlayerList(uuid, username);
            if (isNew) {
                lang.formatList("first-join", Map.of("player-name", username))
                        .forEach(line -> player.sendMessage(mm.deserialize(line)));
            }

            // Info für Admins, wenn sie trotz eines aktiven Bans joinen
            if (player.hasPermission("network.ban.bypass")) {
                Map<String, String> ban = plugin.getMySQLManager().getActiveBan(uuid);
                if (ban != null) {
                    player.sendMessage(mm.deserialize(String.join("<newline>",
                            lang.formatList("ban-bypass-info", Map.of(
                                    "reason", ban.get("reason"),
                                    "duration", ban.get("duration"),
                                    "player-name", username,
                                    "admin-name", ban.getOrDefault("operator", "Unbekannt")
                            )))));
                }
            }
        }).schedule();
    }

    // ==================================================================================
    // 2. VERBINDUNGS-PHASE (SERVERWECHSEL)
    // ==================================================================================

    @Subscribe
    public void onPreServerSwitch(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer target = event.getOriginalServer();
        String name = target.getServerInfo().getName();

        // 1. Forced-Host Check (Nur beim ersten Betreten des Proxys)
        if (player.getCurrentServer().isEmpty()) {
            player.getVirtualHost().ifPresent(host -> {
                if (host.getHostName().contains(".") && !player.hasPermission("network.forcedhost.all") && !player.hasPermission("network.forcedhost." + name)) {
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                    player.disconnect(mm.deserialize(lang.format("no-forcedhost-permission", Map.of("server-name", name))));
                }
            });
            if (!event.getResult().isAllowed()) return;
        }

        // 2. Whitelist Check (Proxy weit oder Gruppen spezifisch)
        if (!isWhitelisted(player, name)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            handleDenial(player, "command-whitelist-kick", Map.of("group", name));
            return;
        }

        // 3. Wartungsarneiten für den Zielserver
        if (plugin.getMySQLManager().isServerInMaintenance(name) && !player.hasPermission("network.maintenance.bypass." + name)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            handleDenial(player, "server-maintenance-no-access", Map.of("server-name", name));
            return;
        }

        // 4. Max Player Check für den Zielserver
        int max = plugin.getMySQLManager().getMaxPlayers(name);
        if (max > 0 && target.getPlayersConnected().size() >= max && !player.hasPermission("network.maxplayers.bypass." + name)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            handleDenial(player, "server-full-message", Map.of("server-name", name, "max", String.valueOf(max)));
        }

        // Tab Cleanup: Wenn der Spieler den Server wechselt, entfernen wir ihn aus den alten Tabs
        if (player.getCurrentServer().isPresent()) {
            removeFromAllTabs(player);
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String serverName = event.getServer().getServerInfo().getName();

        int groupId = plugin.getGroupManager().getGroupId(serverName);
        if (groupId != -1) {
            updateTabHeaderForPlayer(player, groupId);
        }

        server.getScheduler().buildTask(plugin, () -> {
            plugin.getBrandNameChanger().sendBrandName(player, config.getBrandName());
        }).delay(250, TimeUnit.MILLISECONDS).schedule();

        server.getScheduler().buildTask(plugin, () -> {
            plugin.getMySQLManager().updatePlayerServer(player.getUniqueId(), serverName);
            refreshGroupTab(serverName);

        }).delay(Duration.ofMillis(200)).schedule();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player p = event.getPlayer();
        removeFromAllTabs(p);
        plugin.getReplyMap().remove(p.getUniqueId());
        plugin.getReplyMap().values().removeIf(data -> data.partnerId().equals(p.getUniqueId()));
    }

    // ==================================================================================
    // 3. INTERNE LOGIK METHODEN
    // ==================================================================================

    private boolean isWhitelisted(Player p, String serverName) {
        if (p.hasPermission("network.whitelist.bypass")) return true;

        // Globale Whitelist prüfen
        if (plugin.getMySQLManager().isWhitelistActive("proxy")) {
            int gid = plugin.getMySQLManager().getGroupIdByIdentifier("proxy");
            if (!plugin.getMySQLManager().isWhitelisted(p.getUniqueId(), gid)) return false;
        }

        // Spezifische Gruppen Whitelist prüfen
        int gid = plugin.getGroupManager().getGroupId(serverName);
        if (gid != -1) {
            String identifier = plugin.getGroupManager().getGroupIdentifier(gid);
            if (plugin.getMySQLManager().isWhitelistActive(identifier)) {
                return plugin.getMySQLManager().isWhitelisted(p.getUniqueId(), gid);
            }
        }
        return true;
    }

    private void handleDenial(Player p, String langKey, Map<String, String> placeholders) {
        if (p.getCurrentServer().isEmpty()) {
            p.disconnect(mm.deserialize(lang.format(langKey, placeholders)));
        } else {
            p.sendMessage(mm.deserialize(lang.format(langKey, placeholders)));
        }
    }

    // ==================================================================================
    // 4. TABLISTEN-STYLING
    // ==================================================================================

    public void updateTabForGroup(Player player, RegisteredServer connectedServer) {
        int groupId = plugin.getGroupManager().getGroupId(connectedServer.getServerInfo().getName());
        if (groupId == -1) return;

        List<Player> groupPlayers = server.getAllPlayers().stream()
                .filter(p -> p.getCurrentServer().isPresent())
                .filter(p -> plugin.getGroupManager().getGroupId(p.getCurrentServer().get().getServerInfo().getName()) == groupId)
                .collect(Collectors.toList());

        for (Player member : groupPlayers) {
            for (Player networkPlayer : groupPlayers) {
                String prefix = plugin.getLuckpermsUtils().getPlayerPrefix(networkPlayer);
                int weight = plugin.getLuckpermsUtils().getWeight(networkPlayer);

                String displayNameRaw = lang.format("tab-player-format", Map.of(
                        "rank-prefix", prefix != null ? prefix : "",
                        "player-name", networkPlayer.getUsername()
                ));

                TabListEntry entry = TabListEntry.builder()
                        .profile(networkPlayer.getGameProfile())
                        .tabList(member.getTabList())
                        .displayName(mm.deserialize(displayNameRaw))
                        .latency((int) networkPlayer.getPing())
                        .listOrder(weight)
                        .build();

                member.getTabList().removeEntry(networkPlayer.getUniqueId());
                member.getTabList().addEntry(entry);
            }
        }
    }

    private void updateTabEntryPings(Player viewer) {
        for (TabListEntry entry : viewer.getTabList().getEntries()) {
            server.getPlayer(entry.getProfile().getId()).ifPresent(target -> {
                if (entry.getLatency() != (int) target.getPing()) {
                    entry.setLatency((int) target.getPing());
                }
            });
        }
    }

    public void updateTabHeaderForPlayer(Player p, int groupId) {
        String groupName = plugin.getGroupManager().getGroupName(groupId);
        List<String> serversInGroup = plugin.getGroupManager().getServersInGroup(groupId);

        String currentServerInternal = p.getCurrentServer().map(conn -> conn.getServerInfo().getName()).orElse("Unbekannt");
        String currentServerDisplay = plugin.getMySQLManager().getServerDisplayName(currentServerInternal);
        String serverNameToShow = (currentServerDisplay != null) ? currentServerDisplay : currentServerInternal;

        String infoLine = (serversInGroup.size() <= 1) ? "(Lokal)" : groupName + " (Netzwerk)";

        Map<String, String> placeholders = Map.of(
                "group-info", infoLine,
                "group-name", groupName,
                "server-name", serverNameToShow,
                "ping", String.valueOf(p.getPing())
        );

        p.sendPlayerListHeaderAndFooter(
                mm.deserialize(String.join("\n", lang.formatList("tab-header", placeholders))),
                mm.deserialize(String.join("\n", lang.formatList("tab-footer", placeholders)))
        );
    }

    private void removeFromAllTabs(Player playerToRemove) {
        for (Player all : server.getAllPlayers()) {
            all.getTabList().removeEntry(playerToRemove.getUniqueId());
        }
    }

    private void refreshGroupTab(String serverName) {
        int groupId = plugin.getGroupManager().getGroupId(serverName);
        if (groupId == -1) return;

        List<Player> groupPlayers = server.getAllPlayers().stream()
                .filter(p -> p.getCurrentServer().isPresent())
                .filter(p -> plugin.getGroupManager().getGroupId(p.getCurrentServer().get().getServerInfo().getName()) == groupId)
                .collect(Collectors.toList());

        for (Player p : groupPlayers) {
            p.getCurrentServer().ifPresent(conn -> updateTabForGroup(p, conn.getServer()));
        }
    }
}