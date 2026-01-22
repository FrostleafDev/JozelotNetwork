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

    /**
     * Will get finished when I start with the backend plugins.
     * You cant cancel the PlayerChatEvent on the proxy :(
     */
    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {

    }
}