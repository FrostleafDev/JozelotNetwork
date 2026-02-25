package de.jozelot.jozelotLobby;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkStateManager {
    private final Map<String, ServerInfo> cache = new ConcurrentHashMap<>();

    public void updateAll(Map<String, String> newData) {
        newData.forEach((name, rawData) -> {
            String[] parts = rawData.split(":");
            if (parts.length == 2) {
                int players = Integer.parseInt(parts[0]);
                boolean online = parts[1].equalsIgnoreCase("online");
                cache.put(name, new ServerInfo(players, online));
            }
        });
    }

    public ServerInfo getServer(String name) {
        return cache.getOrDefault(name, new ServerInfo(0, false));
    }

    public static record ServerInfo(int players, boolean online) {}
}
