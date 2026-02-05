package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public class PlayerChatListener implements Listener {

    private final JozelotUtils plugin;

    public PlayerChatListener(JozelotUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        if (plugin.getConfigManager().isChatDisabled()) {
            event.setCancelled(true);
        }
    }
}
