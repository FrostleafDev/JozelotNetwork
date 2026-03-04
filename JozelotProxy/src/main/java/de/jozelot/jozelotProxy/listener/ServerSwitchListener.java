package de.jozelot.jozelotProxy.listener;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.ServerLink;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
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

    private final Map<UUID, String> playerSecretStats = new HashMap<>();

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

        server.getScheduler().buildTask(plugin, () -> {
            for (Player viewer : server.getAllPlayers()) {
                // 1. Pings der Köpfe im Tab (TabListEntries)
                updateTabEntryPings(viewer);

                // 2. Header/Footer (Servername, Spieleranzahl, eigener Ping)
                viewer.getCurrentServer().ifPresent(conn -> {
                    int groupId = plugin.getGroupManager().getGroupId(conn.getServerInfo().getName());
                    if (groupId != -1) {
                        updateTabHeaderForPlayer(viewer, groupId);
                        // 3. Namen & Vanish-Status
                        updateTabForGroup(viewer, conn.getServer());
                    }
                });
            }
        }).repeat(Duration.ofSeconds(config.getTabRefreshTime())).schedule();
    }



    // ==================================================================================
    // 1. LOGIN-PHASE (VOR DEM BEITRITT)
    // ==================================================================================

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // 1. Ban-Check
        Map<String, String> banInfo = plugin.getMySQLManager().getActiveBan(uuid);
        if (banInfo != null && !player.hasPermission("network.ban.bypass")) {
            event.setResult(ResultedEvent.ComponentResult.denied(mm.deserialize(
                    String.join("<newline>", lang.formatList("ban-join-screen", banInfo)))));
            return;
        }

        // 2. Netzwerk-Wartung
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

        // 4. Proxy-Kapazität
        int maxProxy = plugin.getMySQLManager().getMaxPlayers("proxy");
        if (maxProxy > 0 && server.getPlayerCount() >= maxProxy && !player.hasPermission("network.maxplayers.bypass.proxy")) {
            event.setResult(ResultedEvent.ComponentResult.denied(mm.deserialize(
                    String.join("<newline>", lang.formatList("proxy-full-kick", Map.of("max", String.valueOf(maxProxy)))))));
        }
    }

    @Subscribe
    public void onServerKick(KickedFromServerEvent event) {
        Player player = event.getPlayer();
        if (player.getCurrentServer().isEmpty()) return;

        String currentServerId = event.getServer().getServerInfo().getName();
        if (currentServerId.equalsIgnoreCase(config.getLobbyServer())) return;

        Optional<RegisteredServer> lobby = server.getServer(config.getLobbyServer());
        if (lobby.isPresent()) {
            String displayServerName = plugin.getMySQLManager().getServerDisplayName(currentServerId);
            String finalServerName = (displayServerName != null) ? displayServerName : currentServerId;
            String kickReason = event.getServerKickReason().map(mm::serialize).orElse("Unbekannter Fehler");

            Map<String, String> placeholders = Map.of("server", finalServerName, "reason", kickReason);
            event.setResult(KickedFromServerEvent.RedirectPlayer.create(lobby.get()));

            server.getScheduler().buildTask(plugin, () -> {
                player.sendMessage(mm.deserialize(String.join("<newline>", lang.formatList("server-kick-redirect", placeholders))));
                playSound(player, "error");
            }).delay(500, TimeUnit.MILLISECONDS).schedule();
        }
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String username = player.getUsername();

        List<ServerLink> links = config.getServerLinks();
        if (!links.isEmpty()) player.setServerLinks(links);

        server.getScheduler().buildTask(plugin, () -> {
            plugin.getBrandNameChanger().sendBrandName(player, config.getBrandName());
        }).delay(250, TimeUnit.MILLISECONDS).schedule();

        int protocolReco = config.getProtocalReco();
        String versionReco = ProtocolVersion.getProtocolVersion(protocolReco).getName();
        int protocolVersion = player.getProtocolVersion().getProtocol();
        String versionPlayer = ProtocolVersion.getProtocolVersion(protocolVersion).getName();
        int protocolMax = config.getProtocalMax();
        String versionMax = ProtocolVersion.getProtocolVersion(protocolMax).getName();

        server.getScheduler().buildTask(plugin, () -> {
            boolean isNew = plugin.getMySQLManager().addToPlayerList(uuid, username, player.getRemoteAddress().getAddress().getHostAddress());

            int found = plugin.getMySQLManager().getFoundSecretsCount(uuid);
            int max = plugin.getMySQLManager().getTotalSecretsCount();
            updatePlayerSecrets(uuid, found, max);

            // 1. Vanish Status laden & setzen
            boolean wasVanished = plugin.getMySQLManager().getVanishStatus(uuid);
            if (wasVanished) {
                // Korrektur: Nutze die neue Methode statt .getVanishedPlayers().add()
                plugin.getVanishManager().setVanished(uuid, false);
                plugin.getRedisManager().publish("network:vanish", uuid + ":true:false");
                refreshGroupTab(null);
            }

            // 2. Spy Status laden & setzen
            boolean isSpy = plugin.getMySQLManager().getSpyStatus(uuid) && player.hasPermission("network.command.spy");
            if (isSpy) {
                plugin.getSpyPlayers().add(uuid);
            } else if (plugin.getMySQLManager().getSpyStatus(uuid)) {
                plugin.getMySQLManager().setSpyStatus(uuid, false);
            }

            // 3. Action Bar Logik (Null-Safe & Sequenziell)
            if (wasVanished && isSpy) {
                player.sendActionBar(mm.deserialize(lang.format("command-vanish-enabled", Map.of("player", username))));
                server.getScheduler().buildTask(plugin, () -> {
                    player.sendActionBar(mm.deserialize(lang.format("command-spy-enabled", Map.of())));
                }).delay(3, TimeUnit.SECONDS).schedule();
            } else if (wasVanished) {
                player.sendActionBar(mm.deserialize(lang.format("command-vanish-enabled", Map.of("player", username))));
            } else if (isSpy) {
                player.sendActionBar(mm.deserialize(lang.format("command-spy-enabled", Map.of())));
            }

            if (isNew) {
                lang.formatList("first-join", Map.of("player-name", username))
                        .forEach(line -> player.sendMessage(mm.deserialize(line)));
            }

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

            if (protocolVersion < protocolReco) {
                player.sendMessage(mm.deserialize(String.join("<newline>", lang.formatList("protocol-recomment", Map.of("current-version", versionPlayer, "reco-version", versionReco, "max-version", versionMax)))));
            }
        }).schedule();

        plugin.getLoginTimes().put(uuid, System.currentTimeMillis());
    }

    // ==================================================================================
    // 2. VERBINDUNGS-PHASE (SERVERWECHSEL)
    // ==================================================================================

    @Subscribe
    public void onPreServerSwitch(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer target = event.getOriginalServer();
        String name = target.getServerInfo().getName();

        if (player.getCurrentServer().isEmpty()) {
            player.getVirtualHost().ifPresent(host -> {
                if (host.getHostName().contains(".") && !player.hasPermission("network.forcedhost.all") && !player.hasPermission("network.forcedhost." + name)) {
                    event.setResult(ServerPreConnectEvent.ServerResult.denied());
                    player.disconnect(mm.deserialize(lang.format("no-forcedhost-permission", Map.of("server-name", name))));
                }
            });
            if (!event.getResult().isAllowed()) return;
        }

        if (!isWhitelisted(player, name)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            handleDenial(player, "command-whitelist-kick", Map.of("group", name));
            return;
        }

        if (plugin.getMySQLManager().isServerInMaintenance(name) && !player.hasPermission("network.maintenance.bypass." + name)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            handleDenial(player, "server-maintenance-no-access", Map.of("server-name", name));
            return;
        }

        int max = plugin.getMySQLManager().getMaxPlayers(name);
        if (max > 0 && target.getPlayersConnected().size() >= max && !player.hasPermission("network.maxplayers.bypass." + name)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            handleDenial(player, "server-full-message", Map.of("server-name", name, "max", String.valueOf(max)));
        }
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String serverName = event.getServer().getServerInfo().getName();

        if (event.getPreviousServer().isPresent()) {
            removeFromAllTabs(player);
        }

        int groupId = plugin.getGroupManager().getGroupId(serverName);
        if (groupId != -1) {
            updateTabHeaderForPlayer(player, groupId);
        }

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            long loginTime = plugin.getLoginTimes().getOrDefault(player.getUniqueId(), System.currentTimeMillis());
            long baseTime = plugin.getMySQLManager().getTotalNetworkPlaytime(player.getUniqueId());
            plugin.getRedisManager().publish("network:playtime", "sync:" + player.getUniqueId() + ":" + baseTime + ":" + loginTime);
        }).delay(1, TimeUnit.SECONDS).schedule();

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
        plugin.getLoginTimes().remove(p.getUniqueId());
    }

    // ==================================================================================
    // 3. INTERNE LOGIK METHODEN
    // ==================================================================================

    private boolean isWhitelisted(Player p, String serverName) {
        if (p.hasPermission("network.whitelist.bypass")) return true;
        if (plugin.getMySQLManager().isWhitelistActive("proxy")) {
            int gid = plugin.getMySQLManager().getGroupIdByIdentifier("proxy");
            if (!plugin.getMySQLManager().isWhitelisted(p.getUniqueId(), gid)) return false;
        }
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

    // ==================================================================================
    // 4. TABLISTEN-STYLING
    // ==================================================================================

    public void updateTabForGroup(Player viewer, RegisteredServer connectedServer) {
        int groupId = plugin.getGroupManager().getGroupId(connectedServer.getServerInfo().getName());
        if (groupId == -1 || !plugin.getGroupManager().isTabEnabled(groupId)) return;

        String groupIdentifier = plugin.getGroupManager().getGroupIdentifier(groupId);

        List<Player> playersInGroup = server.getAllPlayers().stream()
                .filter(p -> p.getCurrentServer().isPresent())
                .filter(p -> plugin.getGroupManager().getGroupId(p.getCurrentServer().get().getServerInfo().getName()) == groupId)
                .collect(Collectors.toList());

        for (Player target : playersInGroup) {
            if (!plugin.getVanishManager().canSee(viewer, target)) {
                viewer.getTabList().removeEntry(target.getUniqueId());
                continue;
            }

            String prefix = plugin.getLuckpermsUtils().getPlayerPrefix(target);
            int weight = plugin.getLuckpermsUtils().getWeight(target);
            String name = target.getUsername();

            String leftSide = lang.format("tab-player-format", Map.of(
                    "rank-prefix", prefix != null ? prefix : "",
                    "player-name", name
            ));

            StringBuilder rightSide = new StringBuilder();
            if (groupIdentifier.equalsIgnoreCase("hub")) {
                // Geändert auf Gold
                rightSide.append(playerSecretStats.getOrDefault(target.getUniqueId(), "<gold>(0/0)"));
            }
            if (plugin.getVanishManager().isVanished(target.getUniqueId())) {
                if (rightSide.length() > 0) rightSide.append(" ");
                rightSide.append("<#00FC00>[V]");
            }

            // Wir nutzen ein spezielles Leerzeichen für breiteren Abstand ohne hässliche Symbole
            String fullDisplayName = "<reset><italic:false>" + leftSide + (rightSide.length() > 0 ? "  " + rightSide : "");

            viewer.getTabList().getEntry(target.getUniqueId()).ifPresentOrElse(entry -> {
                entry.setDisplayName(mm.deserialize(fullDisplayName));
                entry.setLatency((int) target.getPing());
                entry.setListOrder(weight);
            }, () -> {
                viewer.getTabList().addEntry(TabListEntry.builder()
                        .profile(target.getGameProfile())
                        .tabList(viewer.getTabList())
                        .latency((int) target.getPing())
                        .displayName(mm.deserialize(fullDisplayName))
                        .listOrder(weight)
                        .build());
            });
        }

        // Cleanup...
        Set<UUID> groupPlayerUuids = playersInGroup.stream().map(Player::getUniqueId).collect(Collectors.toSet());
        viewer.getTabList().getEntries().forEach(entry -> {
            if (!groupPlayerUuids.contains(entry.getProfile().getId())) {
                viewer.getTabList().removeEntry(entry.getProfile().getId());
            }
        });
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

        long visiblePlayersCount = server.getAllPlayers().stream()
                .filter(target -> plugin.getVanishManager().canSee(p, target))
                .count();

        String infoLine = (serversInGroup.size() <= 1) ? "(Lokal)" : groupName + " (Netzwerk)";

        Map<String, String> placeholders = Map.of(
                "group-info", infoLine,
                "group-name", groupName,
                "server-name", serverNameToShow,
                "online-players", String.valueOf(visiblePlayersCount),
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
        if (serverName == null) {
            updateAllTabs();
            return;
        }

        int groupId = plugin.getGroupManager().getGroupId(serverName);
        if (groupId == -1) return;

        for (Player p : server.getAllPlayers()) {
            p.getCurrentServer().ifPresent(conn -> {
                if (plugin.getGroupManager().getGroupId(conn.getServerInfo().getName()) == groupId) {
                    updateTabForGroup(p, conn.getServer());
                }
            });
        }
    }

    @Subscribe
    public void onPostServerConnect(ServerPostConnectEvent event) {
        playSound(event.getPlayer(), "success");
    }

    private void playSound(CommandSource source, String soundKey) {
        if (!(source instanceof Player player)) return;
        String soundPath = lang.getRaw("sounds." + soundKey);
        if (soundPath == null || soundPath.isEmpty()) return;
        try {
            String cleanedPath = soundPath.trim().toLowerCase();
            if (!cleanedPath.contains(":")) cleanedPath = "minecraft:" + cleanedPath;
            player.playSound(Sound.sound(Key.key(cleanedPath), Sound.Source.UI, 1.0f, 1.0f), Sound.Emitter.self());
        } catch (Exception ignored) {}
    }

    public void updateAllTabs() {
        for (Player all : server.getAllPlayers()) {
            all.getCurrentServer().ifPresent(conn -> {
                updateTabForGroup(all, conn.getServer());
            });
        }
    }

    public void updatePlayerSecrets(UUID uuid, int found, int max) {
        // Wenn gefunden >= max, wird es Gold, sonst bleibt es Grau (oder wie du magst)
        String color = (found >= max && max > 0) ? "<gold>" : "<gray>";
        playerSecretStats.put(uuid, color + "(" + found + "/" + max + ")</reset>");
    }

    public void updateAllPlayerStatsDynamically(int newMax) {
        for (Player p : server.getAllPlayers()) {
            int found = plugin.getMySQLManager().getFoundSecretsCount(p.getUniqueId());
            updatePlayerSecrets(p.getUniqueId(), found, newMax);
        }
        updateAllTabs();
    }
}