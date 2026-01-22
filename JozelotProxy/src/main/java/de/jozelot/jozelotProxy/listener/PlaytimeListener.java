package de.jozelot.jozelotProxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.util.Favicon;
import de.jozelot.jozelotProxy.JozelotProxy;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlaytimeListener {

    private final JozelotProxy plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    public final Map<UUID, Long> sessionStarts = new ConcurrentHashMap<>();

    public PlaytimeListener(JozelotProxy plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String serverName = event.getServer().getServerInfo().getName();

        if (sessionStarts.containsKey(player.getUniqueId())) {
            saveAndRemoveSession(player);
        }

        sessionStarts.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @Subscribe
    public void onProxyDisconnect(DisconnectEvent event) {
        saveAndRemoveSession(event.getPlayer());
    }

    public void saveAndRemoveSession(Player player, boolean synchronous) {
        Long startTime = sessionStarts.remove(player.getUniqueId());
        if (startTime == null) return;

        long durationMillis = System.currentTimeMillis() - startTime;
        if (durationMillis < 1000) return;

        player.getCurrentServer().ifPresent(serverConn -> {
            String serverName = serverConn.getServerInfo().getName();

            if (synchronous) {

                plugin.getMySQLManager().addPlaytime(player.getUniqueId(), serverName, durationMillis);
            } else {
                plugin.getServer().getScheduler().buildTask(plugin, () -> {
                    plugin.getMySQLManager().addPlaytime(player.getUniqueId(), serverName, durationMillis);
                }).schedule();
            }
        });
    }

    public long getCurrentSessionTime(UUID uuid) {
        Long start = sessionStarts.get(uuid);
        if (start == null) return 0;
        return System.currentTimeMillis() - start;
    }

    public void saveAndRemoveSession(Player player) {
        saveAndRemoveSession(player, false);
    }
}
