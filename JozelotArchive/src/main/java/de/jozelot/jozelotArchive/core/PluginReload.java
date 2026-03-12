package de.jozelot.jozelotArchive.core;

import de.jozelot.jozelotArchive.JozelotArchive;

import java.util.Map;

public class PluginReload {

    private final JozelotArchive plugin;
    private final ServiceManager serviceManager;

    public PluginReload(JozelotArchive plugin, ServiceManager serviceManager) {
        this.plugin = plugin;
        this.serviceManager = serviceManager;
    }

    public void reload() {
        plugin.reloadConfig();
        serviceManager.getConfigManager().load();
        serviceManager.getLangManager().load();

        serviceManager.getSQLConnection().close();
        serviceManager.getSQLConnection().setup();

        serviceManager.getRedisConnection().close();
        serviceManager.getRedisConnection().setup();

        Map<String, String> redisData = plugin.getServiceManager().getRedisManager().fetchLanguageData();
        if (redisData != null) {
            serviceManager.getLangManager().integrateRedisData(redisData);
        }

        serviceManager.getUserManager().removeAllUsers();
        serviceManager.getUserManager().registerAllUsers();
    }
}

//hiiiiiiii
//monte man yeees
//was geht
//alles gut bei dir?
//ich bins, tim gioh
//der neue in der truppe
//ich darf auch mal mitspielen heute