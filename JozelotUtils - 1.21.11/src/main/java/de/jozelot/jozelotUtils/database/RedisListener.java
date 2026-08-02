package de.jozelot.jozelotUtils.database;

import de.jozelot.jozelotUtils.JozelotUtils;
import redis.clients.jedis.JedisPubSub;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;

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
                        // Globaler Reload
                        if (channel.equals("network:control") && message.equals("reload")) {
                            handleReload();
                        }

                        // Vanish Update vom Proxy
                        if (channel.equals("network:vanish")) {
                            handleVanishUpdate(message);
                        }
                    }
                }, "network:control", "network:vanish");
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

    private void handleVanishUpdate(String message) {
        try {
            // Format vom Proxy: uuid:state:teamFlag
            String[] parts = message.split(":");
            UUID uuid = UUID.fromString(parts[0]);
            boolean state = Boolean.parseBoolean(parts[1]);
            boolean isTeamVanish = parts.length > 2 && Boolean.parseBoolean(parts[2]);

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getVanishManager().setVanished(uuid, state, isTeamVanish);
                plugin.getLogger().info("Redis: Vanish Update für " + uuid + " (State: " + state + ", Team: " + isTeamVanish + ")");
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Redis: Fehler beim Verarbeiten der Vanish-Nachricht: " + message);
        }
    }
}