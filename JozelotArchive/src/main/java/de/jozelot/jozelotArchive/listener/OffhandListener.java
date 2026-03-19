package de.jozelot.jozelotArchive.listener;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.player.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.UUID;

public class OffhandListener implements Listener {

    private final JozelotArchive plugin;

    public OffhandListener(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        User user = plugin.getServiceManager().getUserManager().getUser(player);

        if (user.canGameModeSwap()) {
            user.cycleGameMode();
        }
        event.setCancelled(true);
    }
}
