package de.jozelot.jozelotArchive.listener;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.core.ServiceManager;
import de.jozelot.jozelotArchive.player.user.User;
import de.jozelot.jozelotArchive.player.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private JozelotArchive plugin;
    private ServiceManager serviceManager;
    private UserManager userManager;

    public ConnectionListener(JozelotArchive plugin) {
        this.plugin = plugin;
        this.serviceManager = plugin.getServiceManager();
        this.userManager = serviceManager.getUserManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        userManager.registerUser(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        userManager.removeUser(player);
    }
}
