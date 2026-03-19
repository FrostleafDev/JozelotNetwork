package de.jozelot.jozelotArchive.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class GameModeChange implements Listener {

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        event.getPlayer().setAllowFlight(true);
        event.getPlayer().setFlying(true);
    }
}
