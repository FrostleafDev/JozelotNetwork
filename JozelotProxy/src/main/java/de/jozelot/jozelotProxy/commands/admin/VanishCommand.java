package de.jozelot.jozelotProxy.commands.admin;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class VanishCommand implements SimpleCommand {

    private final JozelotProxy plugin;
    private final ConsoleLogger consoleLogger;
    private final ConfigManager config;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public VanishCommand(JozelotProxy plugin) {
        this.plugin = plugin;
        this.consoleLogger = plugin.getConsoleLogger();
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player sender)) return;

        String[] args = invocation.arguments();
        Player tempTarget = sender;
        boolean teamFlag = false;

        if (args.length > 0) {
            Optional<Player> optionalTarget = plugin.getServer().getPlayer(args[0]);
            if (optionalTarget.isPresent()) {
                tempTarget = optionalTarget.get();
                if (args.length > 1 && (args[1].equalsIgnoreCase("--team") || args[1].equalsIgnoreCase("-t"))) {
                    teamFlag = true;
                }
            } else if (args[0].equalsIgnoreCase("--team") || args[0].equalsIgnoreCase("-t")) {
                teamFlag = true;
            } else {
                sender.sendMessage(mm.deserialize(plugin.getLang().format("player-not-found", Map.of("player", args[0]))));
                return;
            }
        }

        final Player target = tempTarget;

        if (!target.equals(sender) && !sender.hasPermission("network.command.vanish.other")) {
            sender.sendMessage(mm.deserialize(plugin.getLang().getNoPermission()));
            return;
        }

        // Toggle im Manager
        plugin.getVanishManager().toggleVanish(target, teamFlag);

        boolean nowVanished = plugin.getVanishManager().isVanished(target.getUniqueId());
        String displayState = nowVanished ? "aktiviert" : "deaktiviert";
        String langSuffix = nowVanished ? "enabled" : "disabled";

        // DATABASE LOGGING
        plugin.getMySQLManager().logAction(
                sender.getUniqueId(),
                "VANISH",
                target.getUsername(),
                "Status: " + displayState + (teamFlag ? " | Team-Modus: JA" : "")
        );

        // CONSOLE LOGGING
        consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + sender.getUsername() +
                "<" + config.getColorPrimary() + "> hat den Vanish für " + target.getUsername() +
                " " + displayState + ". (Team-Modus: " + teamFlag + ")");

        // Feedback
        sender.sendActionBar(mm.deserialize(plugin.getLang().format("command-vanish-" + langSuffix,
                Map.of("player", target.getUsername()))));

        if (!target.equals(sender)) {
            target.sendActionBar(mm.deserialize(plugin.getLang().format("command-vanish-target-" + langSuffix,
                    Map.of("admin", sender.getUsername()))));
        }

        String logMsg = plugin.getLang().format("command-vanish-log", Map.of(
                "player", target.getUsername(),
                "state", displayState,
                "info", teamFlag ? "(TEAM-MODUS)" : "",
                "executor", sender.getUsername()
        ));

        plugin.getServer().getAllPlayers().stream()
                .filter(p -> p.hasPermission("network.vanish.see-all") && !p.equals(sender) && !p.equals(target))
                .forEach(p -> p.sendMessage(mm.deserialize(logMsg)));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> suggestions = new ArrayList<>();

        if (args.length <= 1) {
            String input = args.length == 1 ? args[0].toLowerCase() : "";
            suggestions.addAll(plugin.getServer().getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList()));
            if ("--team".startsWith(input)) suggestions.add("--team");
        } else if (args.length == 2) {
            if ("--team".startsWith(args[1].toLowerCase())) {
                suggestions.add("--team");
            }
        }

        return suggestions;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.ban");
    }
}