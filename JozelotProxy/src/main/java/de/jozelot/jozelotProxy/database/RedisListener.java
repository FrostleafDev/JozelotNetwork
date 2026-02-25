package de.jozelot.jozelotProxy.database;

import de.jozelot.jozelotProxy.JozelotProxy;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.UUID;

public class RedisListener {

    private final JozelotProxy plugin;

    public RedisListener(JozelotProxy plugin) {
        this.plugin = plugin;
        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                plugin.getRedisSetup().getJedis().subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        if (channel.equals("move_player")) {
                            // String wieder zerlegen
                            String[] parts = message.split(";");

                            if (parts.length == 2) {
                                UUID uuid = UUID.fromString(parts[0]);
                                String targetServer = parts[1];

                                plugin.getPlayerSends().connectPlayerSimple(uuid, targetServer);
                            }
                        }
                    }
                }, "network:control");
            } catch (Exception e) {
            }
        }, "Redis-Listener-Thread").start();
    }
}
