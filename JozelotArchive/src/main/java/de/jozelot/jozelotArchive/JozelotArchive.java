package de.jozelot.jozelotArchive;

import de.jozelot.jozelotArchive.core.ServiceManager;
import de.jozelot.jozelotArchive.utility.PluginMessages;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class JozelotArchive extends JavaPlugin {

    private ServiceManager serviceManager;

    @Override
    public void onEnable() {
        getLogger().log(Level.INFO, "Plugin wird gestartet...");
        serviceManager = new ServiceManager(this);

        // Checks if all plugins that this plugin depends on are loaded
        if (!serviceManager.checkForDependencies()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        serviceManager.initialize();
        serviceManager.enable();

        PluginMessages.sendStartup(this);
    }

    @Override
    public void onDisable() {
        serviceManager.shutdown();
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }
}
