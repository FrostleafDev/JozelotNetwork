package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

        if (plugin.getVanishManager().isVanished(player.getUniqueId())) {
            event.setQuitMessage(null);
            return;
        }

        handleQuitMessage(event, player);
    }

    private void handleQuitMessage(PlayerQuitEvent event, Player player) {
        String type = config.getLeaveMessageType();

        if (type.equalsIgnoreCase("disabled")) {
            event.setQuitMessage(null);
        } else if (type.equalsIgnoreCase("custom")) {
            String rawMsg = lang.format("leave-message", Map.of("player-name", player.getName()));
            if (rawMsg != null && !rawMsg.isEmpty()) {
                Component component = mm.deserialize(rawMsg);
                // Umwandlung für 1.16.5 API
                String legacyMsg = LegacyComponentSerializer.legacySection().serialize(component);
                event.setQuitMessage(legacyMsg);
            } else {
                event.setQuitMessage(null);
            }
        }
    }
}