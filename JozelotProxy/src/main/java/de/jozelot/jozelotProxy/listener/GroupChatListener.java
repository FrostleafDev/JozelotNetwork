package de.jozelot.jozelotProxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import de.jozelot.jozelotProxy.JozelotProxy;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;

public class GroupChatListener {

    private final JozelotProxy plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public GroupChatListener(JozelotProxy plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String rawMessage = event.getMessage();

        if (rawMessage.startsWith("/")) return;

        if (player.getCurrentServer().isEmpty()) return;
        String serverName = player.getCurrentServer().get().getServerInfo().getName();
        int groupId = plugin.getGroupManager().getGroupId(serverName);

        if (groupId == -1 || !plugin.getGroupManager().isTabEnabled(groupId)) {
            return;
        }

        String processedMessage = rawMessage;
        if (!player.hasPermission("network.chat.minimessage")) {
            processedMessage = mm.escapeTags(rawMessage);
        }

        String prefix = plugin.getLuckpermsUtils().getPlayerPrefix(player);
        String format = plugin.getLang().format("chat-format", Map.of(
                "rank-prefix", prefix != null ? prefix : "",
                "player-name", player.getUsername(),
                "message", processedMessage
        ));

        plugin.getServer().getAllPlayers().stream()
                .filter(p -> p.getCurrentServer().isPresent())
                .filter(p -> plugin.getGroupManager().getGroupId(p.getCurrentServer().get().getServerInfo().getName()) == groupId)
                .forEach(p -> p.sendMessage(mm.deserialize(format)));
    }
}