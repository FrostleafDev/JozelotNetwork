package de.jozelot.jozelotProxy.utils;

import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;

public class PluginReload {

    private final ConfigManager config;
    private final LangManager lang;

    public PluginReload(JozelotProxy plugin) {
        this.config = plugin.getConfig();
        this.lang = plugin.getLang();
    }

    public void reload() {
        config.reload();
        lang.reload();
    }
}
