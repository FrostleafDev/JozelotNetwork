package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public class LeaveListener implements Listener {

    private final ConfigManager config;
    private final LangManager lang;
    private MiniMessage mm = MiniMessage.miniMessage();

    public LeaveListener(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (config.getLeaveMessageType().equalsIgnoreCase("default")) return;
        else if (config.getLeaveMessageType().equalsIgnoreCase("disabled")) {
            event.setQuitMessage("");
        } else if (config.getLeaveMessageType().equalsIgnoreCase("custom")) {
            event.quitMessage(mm.deserialize(lang.format("leave-message", Map.of("player-name", player.getName()))));
        }
    }
}
