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

        Map<String, String> muteInfo = plugin.getMySQLManager().getActivePunishment(player.getUniqueId(), "MUTE");
        if (muteInfo != null) {
            player.sendMessage(mm.deserialize(String.join("<newline>",
                    plugin.getLang().formatList("mute-chat-info", muteInfo))));
            return;
        }

        Map<String, String> shadowInfo = plugin.getMySQLManager().getActivePunishment(player.getUniqueId(), "SHADOWMUTE");
        if (shadowInfo != null) {

            String prefix = plugin.getLuckpermsUtils().getPlayerPrefix(player);
            String fakeFormat = plugin.getLang().format("chat-format", Map.of(
                    "rank-prefix", prefix != null ? prefix : "",
                    "player-name", player.getUsername(),
                    "message", mm.escapeTags(rawMessage)
            ));
            player.sendMessage(mm.deserialize(fakeFormat));
            return;
        }


        if (player.getCurrentServer().isEmpty()) return;
        String serverName = player.getCurrentServer().get().getServerInfo().getName();
        int groupId = plugin.getGroupManager().getGroupId(serverName);

        if (groupId == -1 || !plugin.getGroupManager().isTabEnabled(groupId)) {
            return;
        }

        String processedMessage = player.hasPermission("network.chat.minimessage")
                ? rawMessage
                : mm.escapeTags(rawMessage);

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

        // Spy-Logik bleibt gleich...
        String groupDisplayName = plugin.getGroupManager().getGroupName(groupId);
        String spyChatFormat = plugin.getLang().format("spy-chat", Map.of(
                "group", groupDisplayName != null ? groupDisplayName : "Unbekannt",
                "player-name", player.getUsername(),
                "message", processedMessage
        ));

        plugin.getServer().getAllPlayers().stream()
                .filter(p -> plugin.getSpyPlayers().contains(p.getUniqueId()))
                .filter(p -> p.getCurrentServer()
                        .map(s -> plugin.getGroupManager().getGroupId(s.getServerInfo().getName()) != groupId)
                        .orElse(true))
                .forEach(p -> p.sendMessage(mm.deserialize(spyChatFormat)));
    }
}