package de.jozelot.jozelotArchive.registry;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.listener.ConnectionListener;
import de.jozelot.jozelotArchive.listener.GameModeChange;
import de.jozelot.jozelotArchive.listener.OffhandListener;

public class ListenerRegistry {

    private final JozelotArchive plugin;

    public ListenerRegistry(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public void register() {
        var pm = plugin.getServer().getPluginManager();

        pm.registerEvents(new ConnectionListener(plugin), plugin);
        // pm.registerEvents(new OffhandListener(plugin), plugin);
        pm.registerEvents(new GameModeChange(), plugin);
    }
}
