package de.jozelot.jozelotUtils.utils;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.listener.PlayerNameTag;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        config.loadConfig();
        lang.load();
        plugin.getRedisSetup().setup();
        Map<String, String> redisData = plugin.getRedisManager().fetchLanguageData();
        if (redisData != null) {
            lang.integrateRedisData(redisData);
        }
        PlayerNameTag pnt = plugin.getPlayerNameTag();

        if (!config.isShowPlayerNameTags()) {
            pnt.clearTeams();
        } else {
            for (Player all : Bukkit.getOnlinePlayers()) {
                pnt.updateNametag(all);
            }
        }
    }
}
