package de.jozelot.jozelotArchive.listener;

import de.jozelot.jozelotArchive.JozelotArchive;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class CommandHiderListener implements Listener {

    private MiniMessage mm = MiniMessage.miniMessage();
    private final JozelotArchive plugin;

    public CommandHiderListener(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerGetsCommands(PlayerCommandSendEvent event) {
        event.getCommands().remove("openmenu");
        event.getCommands().remove("kill");
        event.getCommands().remove("bossbar");
        event.getCommands().remove("team");
        event.getCommands().remove("damage");
        event.getCommands().remove("difficulty");
        event.getCommands().remove("execute");
        event.getCommands().remove("fill");
        event.getCommands().remove("setblock");
        event.getCommands().remove("fillbiome");
        event.getCommands().remove("summon");
        event.getCommands().remove("schedule");
        event.getCommands().remove("scoreboard");
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        Player player = event.getPlayer();

        if (!player.hasPermission("network.admin")) {
            return;
        }
        if (message.startsWith("/kill") ||
                message.startsWith("/bossbar") ||
                message.startsWith("/team") ||
                message.startsWith("/damage") ||
                message.startsWith("/difficulty") ||
                message.startsWith("/execute") ||
                message.startsWith("/fill") ||
                message.startsWith("/setblock") ||
                message.startsWith("/fillbiome") ||
                message.startsWith("/summon") ||
                message.startsWith("/schedule") ||
                message.startsWith("/scoreboard")) {

            event.setCancelled(true);
            player.sendMessage(mm.deserialize(plugin.getServiceManager().getLangManager().format("archive-cant-use-command", null)));
        }
    }
}
