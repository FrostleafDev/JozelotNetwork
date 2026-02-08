package de.jozelot.jozelotProxy.database;

import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import redis.clients.jedis.JedisPooled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisManager {

    private final JozelotProxy plugin;
    private final ConsoleLogger consoleLogger;
    private final String REDIS_KEY = "network:lang";

    public RedisManager(JozelotProxy plugin) {
        this.plugin = plugin;
        this.consoleLogger = plugin.getConsoleLogger();
    }

    public void publish(String channel, String message) {
        JedisPooled jedis = plugin.getRedisSetup().getJedis();
        if (jedis == null) {
            consoleLogger.broadCastToConsole("Redis: Publish fehlgeschlagen - Keine Verbindung!");
            return;
        }

        try {
            jedis.publish(channel, message);
        } catch (Exception e) {
            consoleLogger.broadCastToConsole("Redis: Fehler beim Publishen auf " + channel + ": " + e.getMessage());
        }
    }

    public void uploadLanguage(Map<String, Object> allData) {
        JedisPooled jedis = plugin.getRedisSetup().getJedis();
        if (jedis == null) return;

        Map<String, String> flatData = new HashMap<>();

        recursiveFlatten(flatData, "", allData);

        try {
            jedis.del(REDIS_KEY);
            if (!flatData.isEmpty()) {
                jedis.hset(REDIS_KEY, flatData);
                plugin.getConsoleLogger().broadCastToConsole("Redis: " + flatData.size() + " Einträge synchronisiert.");
            }
        } catch (Exception e) {
            plugin.getConsoleLogger().broadCastToConsole("Redis Fehler: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void recursiveFlatten(Map<String, String> result, String prefix, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                recursiveFlatten(result, key, (Map<String, Object>) value);
            } else if (value instanceof List) {
                result.put(key, String.join("<<line>>", (List<String>) value));
            } else if (value != null) {
                result.put(key, value.toString());
            }
        }
    }

    public void sendReloadSignal() {
        JedisPooled jedis = plugin.getRedisSetup().getJedis();
        if (jedis != null) {
            jedis.publish("network:control", "reload");
            plugin.getConsoleLogger().broadCastToConsole("Redis: Reload-Signal an alle Server gesendet.");
        }
    }
}