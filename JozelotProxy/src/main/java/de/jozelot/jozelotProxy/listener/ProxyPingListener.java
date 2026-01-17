package de.jozelot.jozelotProxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import de.jozelot.jozelotProxy.JozelotProxy;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyPingListener {

    private final JozelotProxy plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private final Map<String, Favicon> faviconCache = new HashMap<>();

    public ProxyPingListener(JozelotProxy plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        boolean proxyMaintenance = plugin.getMySQLManager().isServerInMaintenance("proxy");
        String serverIdentifier;

        if (proxyMaintenance) {
            serverIdentifier = "proxy";
        } else {
            // Ermittlung des Zielservers via VirtualHost (Forced Host oder Subdomain)
            serverIdentifier = event.getConnection().getVirtualHost()
                    .map(host -> {
                        String fullHost = host.getHostName().toLowerCase();
                        Map<String, List<String>> forcedHosts = plugin.getServer().getConfiguration().getForcedHosts();

                        if (forcedHosts.containsKey(fullHost)) {
                            List<String> targets = forcedHosts.get(fullHost);
                            return targets.isEmpty() ? "proxy" : targets.get(0);
                        }

                        if (fullHost.contains(".")) {
                            return fullHost.split("\\.")[0];
                        }
                        return "proxy";
                    }).orElse("proxy");
        }

        // Falls der Server nicht in der DB existiert, Fallback auf Proxy-Einstellungen
        if (!plugin.getMySQLManager().existsInDatabase(serverIdentifier)) {
            serverIdentifier = "proxy";
        }

        // 2. Daten aus Datenbank laden
        String rawMotd = plugin.getMySQLManager().getServerMOTD(serverIdentifier);
        boolean isMaintenance = (serverIdentifier.equals("proxy")) ? proxyMaintenance : plugin.getMySQLManager().isServerInMaintenance(serverIdentifier);
        int maxPlayers = plugin.getMySQLManager().getServerMaxPlayers(serverIdentifier);
        String faviconName = plugin.getMySQLManager().getServerFaviconName(serverIdentifier);

        // 3. Online-Spieler ermitteln
        int onlineCount;
        if (serverIdentifier.equals("proxy")) {
            onlineCount = plugin.getServer().getPlayerCount();
        } else {
            onlineCount = plugin.getServer().getServer(serverIdentifier)
                    .map(server -> server.getPlayersConnected().size())
                    .orElse(0);
        }

        if (rawMotd == null || rawMotd.isEmpty()) return;

        // 4. Ping-Builder vorbereiten
        String formattedMotd = rawMotd.replace("\\n", "<newline>");
        ServerPing.Builder pingBuilder = event.getPing().asBuilder();

        // MOTD und Spielerzahlen setzen
        pingBuilder.description(mm.deserialize(formattedMotd));
        pingBuilder.onlinePlayers(onlineCount);
        pingBuilder.maximumPlayers(maxPlayers);

        // 5. Dynamisches Favicon laden
        if (faviconName != null && !faviconName.isEmpty()) {
            Favicon icon = faviconCache.computeIfAbsent(faviconName, name -> {
                File file = plugin.getConfig().getFaviconDirectory().resolve(name).toFile();
                if (file.exists()) {
                    try {
                        return Favicon.create(file.toPath());
                    } catch (IOException e) {
                        plugin.getConsoleLogger().broadCastToConsole("Fehler beim Laden des Favicons " + name + ": " + e.getMessage());
                    }
                }
                return null;
            });

            if (icon != null) {
                pingBuilder.favicon(icon);
            }
        }

        // 6. Wartungs-Anzeige (Version-String überschreiben)
        if (isMaintenance) {
            pingBuilder.version(new ServerPing.Version(1, plugin.getLang().format("maintenance-version", null)));
        }

        event.setPing(pingBuilder.build());
    }
}