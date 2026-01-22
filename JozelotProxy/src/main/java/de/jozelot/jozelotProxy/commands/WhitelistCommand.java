package de.jozelot.jozelotProxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;
import java.util.stream.Collectors;

public class WhitelistCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final ConfigManager config;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public WhitelistCommand(JozelotProxy plugin) {
        this.server = plugin.getServer();
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.consoleLogger = plugin.getConsoleLogger();
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!invocation.source().hasPermission("network.command.reply")) {
            invocation.source().sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        if (args.length < 2) {
            source.sendMessage(mm.deserialize(lang.format("command-whitelist-usage", Map.of())));
            return;
        }

        String action = args[0].toLowerCase();
        String groupName = args[1].toLowerCase();

        int groupId = plugin.getMySQLManager().getGroupIdByIdentifier(groupName);
        String operatorName = (source instanceof Player p) ? p.getUsername() : "Konsole";
        UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);

        if (groupId == -1) {
            source.sendMessage(mm.deserialize(lang.format("command-whitelist-not-found", Map.of("group", groupName))));
            return;
        }

        switch (action) {
            case "on", "off" -> {
                boolean active = action.equals("on");
                plugin.getMySQLManager().setWhitelistState(groupName, active);
                source.sendMessage(mm.deserialize(lang.format("command-whitelist-state",
                        Map.of("group", groupName, "state", active ? "AN" : "AUS"))));

                String stateText = active ? "aktiviert" : "deaktiviert";
                String logText = "<" + config.getColorSecondary() + ">" + operatorName + "<" + config.getColorPrimary() + "> hat die Whitelist für " + groupName + " " + stateText;
                consoleLogger.broadCastToConsole(logText);

                sendAdminLog("command-whitelist-state-admin", Map.of("player-name", operatorName, "group", groupName, "state", stateText), source);
                plugin.getMySQLManager().logAction(operatorUUID, "WHITELIST_STATE", groupName, "State: " + active);
            }
            case "add" -> {
                if (args.length < 3) return;
                String targetName = args[2];
                UUID targetUUID = plugin.getMySQLManager().getUUIDByUsername(targetName);
                if (targetUUID == null) {
                    Optional<Player> online = server.getPlayer(targetName);
                    if (online.isPresent()) targetUUID = online.get().getUniqueId();
                }

                if (targetUUID == null) {
                    source.sendMessage(mm.deserialize("{danger}Spieler wurde nie auf dem Netzwerk gesehen."));
                    return;
                }

                plugin.getMySQLManager().addToWhitelist(targetUUID, groupId, operatorUUID);
                source.sendMessage(mm.deserialize(lang.format("command-whitelist-added",
                        Map.of("player", targetName, "group", groupName))));

                String logText = "<" + config.getColorSecondary() + ">" + operatorName + "<" + config.getColorPrimary() + "> hat " + targetName + " zur Whitelist von " + groupName + " hinzugefügt";
                consoleLogger.broadCastToConsole(logText);

                sendAdminLog("command-whitelist-added-admin", Map.of("player-name", operatorName, "target", targetName, "group", groupName), source);
                plugin.getMySQLManager().logAction(operatorUUID, "WHITELIST_ADD", targetName, "Group: " + groupName);
            }
            case "remove" -> {
                if (args.length < 3) return;
                String targetName = args[2];
                UUID targetUUID = plugin.getMySQLManager().getUUIDByUsername(targetName);
                if (targetUUID != null) {

                    plugin.getMySQLManager().removeFromWhitelist(targetUUID, groupId);
                    source.sendMessage(mm.deserialize(lang.format("command-whitelist-removed",
                            Map.of("player", targetName, "group", groupName))));
                    String logText = "<" + config.getColorSecondary() + ">" + operatorName + "<" + config.getColorPrimary() + "> hat " + targetName + " von der Whitelist von " + groupName + " entfernt";
                    consoleLogger.broadCastToConsole(logText);

                    sendAdminLog("command-whitelist-removed-admin", Map.of("player-name", operatorName, "target", targetName, "group", groupName), source);
                    plugin.getMySQLManager().logAction(operatorUUID, "WHITELIST_REMOVE", targetName, "Group: " + groupName);
                }
            }
            case "list" -> {
                List<String> names = plugin.getMySQLManager().getWhitelistPlayers(groupId);
                source.sendMessage(mm.deserialize(lang.format("command-whitelist-list-header", Map.of("group", groupName))));
                source.sendMessage(mm.deserialize("<" + plugin.getConfig().getColorGrey() + ">" + String.join(", ", names)));
            }
        }
    }

    private void sendAdminLog(String langKey, Map<String, String> placeholders, CommandSource source) {
        for (Player player : server.getAllPlayers()) {
            if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                player.sendMessage(mm.deserialize(lang.format(langKey, placeholders)));
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (!invocation.source().hasPermission("network.command.reply")) {
            return List.of();
        }

        if (args.length <= 1) {
            return List.of("add", "remove", "list", "on", "off").stream()
                    .filter(s -> s.startsWith(currentArg)).toList();
        }

        if (args.length == 2) {
            List<String> groups = new ArrayList<>(plugin.getGroupManager().getAllGroupIdentifiers());
            groups.add("proxy");
            return groups.stream().filter(s -> s.startsWith(currentArg)).toList();
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(s -> s.toLowerCase().startsWith(currentArg))
                    .toList();
        }

        return List.of();
    }
    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.whitelist");
    }
}