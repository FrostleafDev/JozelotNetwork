package de.jozelot.jozelotProxy.commands.admin;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.util.Map;

public class SpyCommand implements SimpleCommand {

    private final JozelotProxy plugin;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SpyCommand(JozelotProxy plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(mm.deserialize(lang.getOnlyPlayer()));
            return;
        }

        if (!player.hasPermission("network.command.spy")) {
            player.sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        boolean newState = !plugin.getSpyPlayers().contains(player.getUniqueId());

        if (newState) {
            plugin.getSpyPlayers().add(player.getUniqueId());
            player.sendActionBar(mm.deserialize(lang.format("command-spy-enabled", null)));
        } else {
            plugin.getSpyPlayers().remove(player.getUniqueId());
            player.sendActionBar(mm.deserialize(lang.format("command-spy-disabled", null)));
        }

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            plugin.getMySQLManager().setSpyStatus(player.getUniqueId(), newState);
        }).schedule();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.spy");
    }
}