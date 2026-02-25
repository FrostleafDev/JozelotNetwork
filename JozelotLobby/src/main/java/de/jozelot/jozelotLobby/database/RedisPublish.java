package de.jozelot.jozelotLobby.database;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.player.LobbyPlayerManager;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPooled;

import java.util.UUID;

public class RedisPublish {

    private final JozelotLobby plugin;

    public RedisPublish(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    public void sendPlayerToServer(UUID uuid, String serverName) {
        JedisPooled jedis = plugin.getRedisSetup().getJedis();
        if (jedis != null) {
            String message = uuid + ";" + serverName;

            jedis.publish("move_player", message);
        }
    }
}
