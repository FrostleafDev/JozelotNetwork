package de.jozelot.jozelotProxy.database;

import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import redis.clients.jedis.JedisPooled;

public class RedisSetup {

    private final ConfigManager config;
    private JedisPooled jedis;

    public RedisSetup(JozelotProxy plugin) {
        this.config = plugin.getConfig();
    }

    public void setup() {
        String uri = "redis://:" + config.getRedisPassword() + "@" + config.getRedisHost() + ":" + config.getRedisPort();
        this.jedis = new JedisPooled(uri);

        System.out.println("Redis Verbindung hergestellt!");
    }

    public JedisPooled getJedis() {
        return jedis;
    }
}