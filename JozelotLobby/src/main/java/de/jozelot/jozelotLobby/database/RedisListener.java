package de.jozelot.jozelotLobby.database;

import de.jozelot.jozelotLobby.JozelotLobby;
import redis.clients.jedis.JedisPubSub;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;

public class RedisListener {

    private final JozelotLobby plugin;

    public RedisListener(JozelotLobby plugin) {
        this.plugin = plugin;
        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                plugin.getRedisSetup().getJedis().subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        // Globaler Reload
                        if (channel.equals("network:control") && message.equals("reload")) {
                            handleReload();
                        }
                    }
                }, "network:control");
            } catch (Exception e) {
                plugin.getLogger().severe("Redis: Fehler im Pub/Sub Listener: " + e.getMessage());
            }
        }, "Redis-Listener-Thread").start();
    }

    private void handleReload() {
        plugin.getLogger().info("Redis: Globaler Reload empfangen!");
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getReloadPlugin().reload();
            Map<String, String> data = plugin.getRedisManager().fetchLanguageData();
            if (data != null) {
                plugin.getLang().integrateRedisData(data);
            }
            plugin.getLogger().info("Redis: Plugin-Reload abgeschlossen.");
        });
    }

    public void startStatusUpdater() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                Map<String, String> states = plugin.getRedisSetup().getJedis().hgetAll("network_status");

                if (states != null) {
                    plugin.getNetworkStateManager().updateAll(states);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Redis: Fehler beim Update der Server-Status-Daten: " + e.getMessage());
            }
        }, 0L, 100L);
    }
}