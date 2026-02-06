package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class FallOffListener implements Listener {

    private final ConfigManager config;

    public FallOffListener(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!config.isSpawnOnFall()) {
            //event.getPlayer().sendMessage("Spawnfall ist aus");
            return;
        }

        if (event.getTo().getY() == event.getFrom().getY()) {
            return;
        }

        if (event.getTo().getY() <= config.getFallOffHeight()) {
            //event.getPlayer().sendMessage("unter der höhe");
            if (config.getSpawnLocation() != null) {
                //event.getPlayer().sendMessage("teleport");
                Player player = event.getPlayer();
                player.teleport(config.getSpawnLocation());
                player.setFallDistance(0);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.isSpawnOnJoin()) {
            //event.getPlayer().sendMessage("spawn join ist aus");
            return;
        }
        Player player = event.getPlayer();

        if (config.getSpawnLocation() != null) {
            //event.getPlayer().sendMessage("teleport");
            player.teleport(config.getSpawnLocation());
            player.setFallDistance(0);
        }
    }
}
