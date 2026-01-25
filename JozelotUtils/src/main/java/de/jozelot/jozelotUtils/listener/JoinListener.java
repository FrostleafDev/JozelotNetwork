package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class JoinListener implements Listener {

    private final ConfigManager config;

    public JoinListener(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        try {
            GameMode gm = GameMode.valueOf(config.getDefaultGamemode().toUpperCase());
            player.setGameMode(gm);
        } catch (IllegalArgumentException e) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        if (player.hasPermission("network.utils.admin") && config.isAutomaticFlight()) {
            enableFly(player);
        }
        else if (config.isAutomaticFlightPlayer()) {
            enableFly(player);
        }
        else {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    private void enableFly(Player player) {
        player.setAllowFlight(true);
        player.setFlying(true);

        if (player.isOnGround()) {
            player.teleport(player.getLocation().add(0, 0.1, 0));
        }
    }
}
