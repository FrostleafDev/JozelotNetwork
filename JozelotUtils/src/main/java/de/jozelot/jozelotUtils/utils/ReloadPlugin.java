package de.jozelot.jozelotUtils.utils;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;

import java.util.Map;

public class ReloadPlugin {

    private final JozelotUtils plugin;
    private final ConfigManager config;
    private final LangManager lang;

    public ReloadPlugin(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        lang.load();
        plugin.getRedisSetup().setup();
        Map<String, String> redisData = plugin.getRedisManager().fetchLanguageData();
        if (redisData != null) {
            lang.integrateRedisData(redisData);
        }
    }
}
