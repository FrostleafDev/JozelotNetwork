package de.jozelot.jozelotUtils.database;

import de.jozelot.jozelotUtils.JozelotUtils;
import redis.clients.jedis.JedisPubSub;
import org.bukkit.Bukkit;

import java.util.Map;

public class RedisListener {

    private final JozelotUtils plugin;

    public RedisListener(JozelotUtils plugin) {
        this.plugin = plugin;
        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                plugin.getRedisSetup().getJedis().subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        if (channel.equals("network:control") && message.equals("reload")) {
                            plugin.getLogger().info("Redis: Globaler Reload empfangen!");

                            Bukkit.getScheduler().runTask(plugin, () -> {

                                plugin.getReloadPlugin().reload();

                                Map<String, String> data = plugin.getRedisManager().fetchLanguageData();
                                if (data != null) {
                                    plugin.getLang().integrateRedisData(data);
                                }

                                plugin.getLogger().info("Redis: Plugin-Reload (Config & Lang) abgeschlossen.");
                            });
                        }
                    }
                }, "network:control");
            } catch (Exception e) {
                plugin.getLogger().severe("Redis: Fehler im Pub/Sub Listener: " + e.getMessage());
            }
        }, "Redis-Listener-Thread").start();
    }
}