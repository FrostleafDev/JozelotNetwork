package de.jozelot.jozelotProxy.utils;

import com.velocitypowered.api.proxy.Player;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.apis.GroupManager;
import de.jozelot.jozelotProxy.database.MySQLSetup;
import de.jozelot.jozelotProxy.database.RedisSetup;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;

public class PluginReload {

    private final ConfigManager config;
    private final LangManager lang;
    private final MySQLSetup mySQLSetup;
    private final RedisSetup redisSetup;
    private final GroupManager groupManager;
    private final JozelotProxy plugin;

    public PluginReload(JozelotProxy plugin) {
        this.config = plugin.getConfig();
        this.lang = plugin.getLang();
        this.redisSetup = plugin.getRedisSetup();
        this.mySQLSetup = plugin.getMySQLSetup();
        this.groupManager = plugin.getGroupManager();
        this.plugin = plugin;
    }

    public void reload() {
        config.reload();
        lang.reload();
        groupManager.load();
        redisSetup.setup();
        mySQLSetup.setup();
        for (Player player : plugin.getServer().getAllPlayers()) {
            plugin.getBrandNameChanger().sendBrandName(player, config.getBrandName());
        }

    }
}
