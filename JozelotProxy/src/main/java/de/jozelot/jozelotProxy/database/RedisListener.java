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
                        if (channel.equals("network:move")) {
                            // String wieder zerlegen
                            String[] parts = message.split(";");

                            if (parts.length == 2) {
                                UUID uuid = UUID.fromString(parts[0]);
                                String targetServer = parts[1];

                                plugin.getPlayerSends().connectPlayerSimple(uuid, targetServer);
                                //System.out.println("Spieler move empfangen");
                            }
                        }
                        if (channel.equals("network:secrets")) {
                            String[] parts = message.split(":");
                            if (parts.length == 3) {
                                if (parts[0].equals("GLOBAL_UPDATE")) {
                                    int newMax = Integer.parseInt(parts[2]);
                                    // Alle online Spieler im Tab updaten
                                    plugin.getServerSwitchListener().updateAllPlayerStatsDynamically(newMax);
                                } else {
                                    // Normales Spieler-Update
                                    UUID uuid = UUID.fromString(parts[0]);
                                    int found = Integer.parseInt(parts[1]);
                                    int max = Integer.parseInt(parts[2]);
                                    plugin.getServerSwitchListener().updatePlayerSecrets(uuid, found, max);
                                    plugin.getServerSwitchListener().updateAllTabs();
                                }
                            }
                        }
                    }
                }, "network:move", "network:secrets");
            } catch (Exception e) {
            }
        }, "Redis-Listener-Thread").start();
    }
}
