package de.jozelot.jozelotArchive.core.database.redis;

import de.jozelot.jozelotArchive.JozelotArchive;
import redis.clients.jedis.JedisPooled;

import java.util.Map;

public class RedisManager {

    private final JozelotArchive plugin;
    private final String REDIS_KEY = "network:lang";

    public RedisManager(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public Map<String, String> fetchLanguageData() {
        JedisPooled jedis = plugin.getServiceManager().getRedisConnection().getJedis();

        if (jedis == null) {
            plugin.getLogger().warning("Redis: Verbindung nicht aktiv! Nutze lokale Fallback-Daten.");
            return null;
        }

        try {
            Map<String, String> data = jedis.hgetAll(REDIS_KEY);
            if (data == null || data.isEmpty()) {
                plugin.getLogger().warning("Redis: Keine Sprachdaten unter '" + REDIS_KEY + "' gefunden.");
                return null;
            }
            return data;
        } catch (Exception e) {
            plugin.getLogger().severe("Redis: Fehler beim Abrufen der Sprache: " + e.getMessage());
            return null;
        }
    }
}
