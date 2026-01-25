package de.jozelot.jozelotUtils.database;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import redis.clients.jedis.JedisPooled;

public class RedisSetup {

    private final ConfigManager config;
    private JedisPooled jedis;

    public RedisSetup(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
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

        } catch (Exception e) {
            e.printStackTrace();
            this.jedis = null;
        }
    }

    public JedisPooled getJedis() {
        return jedis;
    }
}
