package de.jozelot.jozelotLobby.utils;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.storage.ConfigManager;
import de.jozelot.jozelotLobby.storage.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public class ReloadPlugin {

    private final JozelotLobby plugin;
    private final ConfigManager config;
    private final LangManager lang;

    public ReloadPlugin(JozelotLobby plugin) {
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        config.loadConfig();
        lang.load();
        plugin.getRedisSetup().close();
        plugin.getMySQLSetup().close();
        plugin.getMySQLSetup().setup();
        plugin.getRedisSetup().setup();
        Map<String, String> redisData = plugin.getRedisManager().fetchLanguageData();
        if (redisData != null) {
            lang.integrateRedisData(redisData);
        }
        plugin.getLobbyPlayerManager().removeAllPlayers();
        plugin.getLobbyPlayerManager().registerAllPlayers();
        plugin.getHotbarManager().handleReload();

        plugin.getSecretMgr().reload();
    }
}
