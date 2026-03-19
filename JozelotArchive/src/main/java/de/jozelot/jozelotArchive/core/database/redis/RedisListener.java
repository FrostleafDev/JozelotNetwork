package de.jozelot.jozelotArchive.core.database.redis;

import de.jozelot.jozelotArchive.JozelotArchive;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.UUID;

public class RedisListener {

    private final JozelotArchive plugin;

    public RedisListener(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public void startListening() {
        new Thread(() -> {
            try {
                plugin.getServiceManager().getRedisConnection().getJedis().subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
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
            plugin.getServiceManager().reloadAll();
            Map<String, String> data = plugin.getServiceManager().getRedisManager().fetchLanguageData();
            if (data != null) {
                plugin.getServiceManager().getLangManager().integrateRedisData(data);
            }
            plugin.getLogger().info("Redis: Plugin-Reload abgeschlossen.");
        });
    }
}
