package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public class LeaveListener implements Listener {

    private final JozelotUtils plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public LeaveListener(JozelotUtils plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // 1. Vanish Check: Wenn gevanished, keine Nachricht senden
        if (plugin.getVanishManager().isVanished(player.getUniqueId())) {
            event.quitMessage(null);
            return;
        }

        // 2. Normale Quit-Message Logik
        handleQuitMessage(event, player);
    }

    private void handleQuitMessage(PlayerQuitEvent event, Player player) {
        String type = config.getLeaveMessageType();

        if (type.equalsIgnoreCase("disabled")) {
            event.quitMessage(null);
        } else if (type.equalsIgnoreCase("custom")) {
            String msg = lang.format("leave-message", Map.of("player-name", player.getName()));
            if (msg != null && !msg.isEmpty()) {
                event.quitMessage(mm.deserialize(msg));
            } else {
                event.quitMessage(null);
            }
        }
        // Bei "default" wird nichts geändert, Bukkit nutzt die Standardnachricht
    }
}