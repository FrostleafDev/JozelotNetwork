package de.jozelot.jozelotArchive.registry;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.listener.*;

public class ListenerRegistry {

    private final JozelotArchive plugin;

    public ListenerRegistry(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public void register() {
        var pm = plugin.getServer().getPluginManager();

        pm.registerEvents(new ConnectionListener(plugin), plugin);
        pm.registerEvents(new GameModeChange(plugin), plugin);
        pm.registerEvents(new MenuClickListener(plugin), plugin);
        pm.registerEvents(new CommandHiderListener(plugin), plugin);
        pm.registerEvents(new ContainerOpenListener(), plugin);
        pm.registerEvents(new HotbarItemClickListener(plugin), plugin);

        // pm.registerEvents(new OffhandListener(plugin), plugin);
    }
}
