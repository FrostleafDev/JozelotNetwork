package de.jozelot.jozelotProxy.utils;

import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.database.MySQLSetup;
import de.jozelot.jozelotProxy.database.RedisSetup;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;

public class PluginReload {

    private final ConfigManager config;
    private final LangManager lang;
    private final MySQLSetup mySQLSetup;
    private final RedisSetup redisSetup;

    public PluginReload(JozelotProxy plugin) {
        this.config = plugin.getConfig();
        this.lang = plugin.getLang();
        this.redisSetup = plugin.getRedisSetup();
        this.mySQLSetup = plugin.getMySQLSetup();
    }

    public void reload() {
        config.reload();
        lang.reload();
    }
}
