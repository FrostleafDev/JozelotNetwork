package de.jozelot.jozelotProxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import de.jozelot.jozelotProxy.JozelotProxy;

public class ServerSwitchListener {

    private final JozelotProxy plugin;

    public ServerSwitchListener(JozelotProxy plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String serverName = event.getServer().getServerInfo().getName();

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            plugin.getMySQLManager().updatePlayerServer(player.getUniqueId(), serverName);
        }).schedule();
    }
}
