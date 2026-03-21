package de.jozelot.jozelotArchive.registry;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.commands.OpenMenuCommand;

public class CommandRegistry {

    private final JozelotArchive plugin;

    public CommandRegistry(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getCommand("openmenu").setExecutor(new OpenMenuCommand(plugin));
    }
}
