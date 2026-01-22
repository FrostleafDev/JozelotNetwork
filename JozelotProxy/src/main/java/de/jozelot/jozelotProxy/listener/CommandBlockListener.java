package de.jozelot.jozelotProxy.listener;

import com.mojang.brigadier.tree.RootCommandNode;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;

public class CommandBlockListener {

    private final JozelotProxy plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private MiniMessage mm = MiniMessage.miniMessage();

    public CommandBlockListener(JozelotProxy plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.lang = plugin.getLang();
    }

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player player)) return;

        String rawCommand = event.getCommand().toLowerCase();
        String command = rawCommand.split(" ")[0];
        if (!command.startsWith("/")) command = "/" + command;

        if (command.contains(":")) {
            player.sendMessage(mm.deserialize(lang.format("blocked-command", Map.of("command", command))));
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        if (config.getHardBlocked().contains(command)) {
            player.sendMessage(mm.deserialize(lang.format("blocked-command", Map.of("command", command))));
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        Map<String, List<String>> groups = config.getCommandGroups();
        for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
            String groupName = entry.getKey();
            List<String> blockedCommands = entry.getValue();

            if (blockedCommands.contains(command)) {
                String permission = "network.commands.bypass." + groupName;
                if (!player.hasPermission(permission)) {
                    player.sendMessage(mm.deserialize(lang.format("blocked-command", Map.of("command", command))));
                    event.setResult(CommandExecuteEvent.CommandResult.denied());
                    return;
                }
            }
        }
    }

    @Subscribe
    public void onTabComplete(TabCompleteEvent event) {

        Player player = event.getPlayer();

        List<String> suggestions = event.getSuggestions();

        suggestions.removeIf(suggestion -> {
            String cmd = suggestion.toLowerCase();
            if (!cmd.startsWith("/")) cmd = "/" + cmd;

            if (config.getHardBlocked().contains(cmd)) return true;

            for (Map.Entry<String, List<String>> entry : config.getCommandGroups().entrySet()) {
                if (entry.getValue().contains(cmd)) {
                    return !player.hasPermission("network.commands.bypass." + entry.getKey());
                }
            }

            if (cmd.contains(":")) return true;

            return false;
        });
    }

    @Subscribe
    public void onPlayerAvailableCommands(PlayerAvailableCommandsEvent event) {
        Player player = event.getPlayer();

        RootCommandNode<?> rootNode = event.getRootNode();

        rootNode.getChildren().removeIf(node -> {
            String commandName = node.getName().toLowerCase();
            String commandWithSlash = "/" + commandName;

            if (config.getHardBlocked().contains(commandWithSlash)) {
                return true;
            }

            Map<String, List<String>> groups = config.getCommandGroups();
            for (Map.Entry<String, List<String>> entry : groups.entrySet()) {
                if (entry.getValue().contains(commandWithSlash)) {
                    return !player.hasPermission("network.commands.bypass." + entry.getKey());
                }
            }

            if (commandName.contains(":")) {
                return true;
            }

            return false;
        });
    }
}
