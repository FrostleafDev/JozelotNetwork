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

    public void uploadLanguage(Map<String, Object> allMessages) {
        JedisPooled jedis = plugin.getRedisSetup().getJedis();

        if (jedis == null) {
            consoleLogger.broadCastToConsole("Redis: Upload fehlgeschlagen - Keine aktive Verbindung!");
            return;
        }

        if (allMessages == null || allMessages.isEmpty()) {
            consoleLogger.broadCastToConsole("Redis: Keine Sprach-Daten zum Hochladen gefunden.");
            return;
        }

        Map<String, String> dataToUpload = new HashMap<>();

        for (Map.Entry<String, Object> entry : allMessages.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof String) {
                dataToUpload.put(key, (String) value);
            } else if (value instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> list = (List<String>) value;
                dataToUpload.put(key, String.join("<<line>>", list));
            }
        }

        try {
            jedis.del(REDIS_KEY);
            jedis.hset(REDIS_KEY, dataToUpload);
            consoleLogger.broadCastToConsole("Redis: " + dataToUpload.size() + " Sprach-Einträge erfolgreich synchronisiert.");
        } catch (Exception e) {
            consoleLogger.broadCastToConsole("Redis: Fehler beim Sprach-Upload: " + e.getMessage());
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