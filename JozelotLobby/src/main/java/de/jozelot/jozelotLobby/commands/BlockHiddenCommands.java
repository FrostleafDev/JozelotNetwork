package de.jozelot.jozelotLobby.commands;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class BlockHiddenCommands implements Listener {

    @EventHandler
    public void onPlayerGetsCommands(PlayerCommandSendEvent event) {
        event.getCommands().remove("openmenu");
    }
}
