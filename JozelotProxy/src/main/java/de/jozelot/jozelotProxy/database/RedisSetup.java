package de.jozelot.jozelotProxy.database;

import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import redis.clients.jedis.JedisPooled;

public class RedisSetup {

    private final ConfigManager config;
    private final ConsoleLogger consoleLogger;
    private JedisPooled jedis;

    public RedisSetup(JozelotProxy plugin) {
        this.config = plugin.getConfig();
        this.consoleLogger = plugin.getConsoleLogger();
    }

    public void setup() {
        String host = config.getRedisHost();
        int port = config.getRedisPort();
        String pass = config.getRedisPassword();

        try {
            if (pass != null && !pass.isEmpty()) {
                this.jedis = new JedisPooled(host, port, null, pass);
            } else {
                this.jedis = new JedisPooled(host, port);
            }

            String response = jedis.ping();

            if (response.equalsIgnoreCase("PONG")) {
                consoleLogger.broadCastToConsole("Redis: Verbindung erfolgreich hergestellt! (PONG)");
            }
        } catch (Exception e) {
            consoleLogger.broadCastToConsole("Redis: Fehler beim Verbindungsaufbau: " + e.getMessage());
            e.printStackTrace();
            this.jedis = null;
        }
    }

    public JedisPooled getJedis() {
        return jedis;
    }
}