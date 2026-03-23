package de.jozelot.jozelotArchive.listener;

import de.jozelot.jozelotArchive.JozelotArchive;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class GameModeChange implements Listener {

    private JozelotArchive plugin;

    public GameModeChange(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setAllowFlight(true);
            player.setFlying(true);
        }, 1L);
    }
}
