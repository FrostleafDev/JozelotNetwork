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
        String serverIdentifier = event.getConnection().getVirtualHost()
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

        if (!plugin.getMySQLManager().existsInDatabase(serverIdentifier)) {
            serverIdentifier = "proxy";
        }

        String rawMotd = plugin.getMySQLManager().getServerMOTD(serverIdentifier);
        int maxPlayers = plugin.getMySQLManager().getServerMaxPlayers(serverIdentifier);
        String faviconName = plugin.getMySQLManager().getServerFaviconName(serverIdentifier);

        boolean proxyMaintenance = plugin.getMySQLManager().isServerInMaintenance("proxy");
        boolean specificMaintenance = plugin.getMySQLManager().isServerInMaintenance(serverIdentifier);

        boolean showMaintenanceMode = proxyMaintenance || specificMaintenance;

        int onlineCount = serverIdentifier.equals("proxy")
                ? plugin.getServer().getPlayerCount()
                : plugin.getServer().getServer(serverIdentifier).map(s -> s.getPlayersConnected().size()).orElse(0);

        if (rawMotd == null || rawMotd.isEmpty()) return;

        ServerPing.Builder pingBuilder = event.getPing().asBuilder();
        pingBuilder.description(mm.deserialize(rawMotd.replace("\\n", "<newline>")));
        pingBuilder.onlinePlayers(onlineCount);
        pingBuilder.maximumPlayers(maxPlayers);

        if (faviconName != null && !faviconName.isEmpty()) {
            Favicon icon = faviconCache.computeIfAbsent(faviconName, name -> {
                File file = plugin.getConfig().getFaviconDirectory().resolve(name).toFile();
                if (file.exists()) {
                    try { return Favicon.create(file.toPath()); }
                    catch (IOException e) { e.printStackTrace(); }
                }
                return null;
            });
            if (icon != null) pingBuilder.favicon(icon);
        }

        if (showMaintenanceMode) {
            pingBuilder.version(new ServerPing.Version(1, plugin.getLang().format("maintenance-version", null)));
        }

        event.setPing(pingBuilder.build());
    }
}