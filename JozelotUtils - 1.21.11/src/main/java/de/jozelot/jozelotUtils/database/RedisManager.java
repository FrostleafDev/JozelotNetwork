package de.jozelot.jozelotUtils.database;

import de.jozelot.jozelotUtils.JozelotUtils;
import redis.clients.jedis.JedisPooled;
import java.util.Map;

public class RedisManager {

    private final JozelotUtils plugin;
    private final String REDIS_KEY = "network:lang";

    public RedisManager(JozelotUtils plugin) {
        this.plugin = plugin;
    }

    public Map<String, String> fetchLanguageData() {
        JedisPooled jedis = plugin.getRedisSetup().getJedis();

        if (jedis == null) {
            plugin.getLogger().warning("Redis: Verbindung nicht aktiv! Nutze lokale Fallback-Daten.");
            return null;
        }

        try {
            // Holt den kompletten Hash aus Redis
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