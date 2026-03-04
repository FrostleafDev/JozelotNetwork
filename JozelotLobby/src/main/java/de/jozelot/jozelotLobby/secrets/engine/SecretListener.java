package de.jozelot.jozelotLobby.secrets.engine;

import de.jozelot.jozelotLobby.JozelotLobby;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class SecretListener implements Listener {

    private final JozelotLobby plugin;

    public SecretListener(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getTo();

        plugin.getSecretMgr().checkLocation(player, loc);
    }
}
