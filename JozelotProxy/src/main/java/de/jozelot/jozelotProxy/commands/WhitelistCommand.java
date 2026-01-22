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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

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

        if (args.length < 2) {
            source.sendMessage(mm.deserialize(lang.format("command-whitelist-usage", Map.of())));
            return;
        }

        String action = args[0].toLowerCase();
        String groupName = args[1].toLowerCase();
        int groupId = plugin.getMySQLManager().getGroupIdByIdentifier(groupName);

        if (groupId == -1 && !groupName.equals("proxy")) {
            source.sendMessage(mm.deserialize(lang.format("command-whitelist-not-found", Map.of("group", groupName))));
            return;
        }

        String operatorName = (source instanceof Player p) ? p.getUsername() : "Konsole";
        UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);

        switch (action) {
            case "on", "off" -> handleToggle(source, groupName, action.equals("on"), operatorName, operatorUUID);
            case "add" -> {
                if (args.length < 3) return;
                String targetName = args[2];

                CompletableFuture.supplyAsync(() -> fetchUUID(targetName)).thenAccept(targetUUID -> {
                    if (targetUUID == null) {
                        source.sendMessage(mm.deserialize(lang.format("command-whitelist-player-not-found", Map.of("player-name", targetName))));
                        return;
                    }

                    plugin.getMySQLManager().addToWhitelist(targetUUID, groupId, operatorUUID);
                    source.sendMessage(mm.deserialize(lang.format("command-whitelist-added", Map.of("player", targetName, "group", groupName))));

                    logAction(operatorName, operatorUUID, "WHITELIST_ADD", targetName, groupName, "hinzugefügt");
                    sendAdminLog("command-whitelist-added-admin", Map.of("player-name", operatorName, "target", targetName, "group", groupName), source);
                });
            }
            case "remove" -> {
                if (args.length < 3) return;
                String targetName = args[2];
                CompletableFuture.supplyAsync(() -> fetchUUID(targetName)).thenAccept(targetUUID -> {
                    if (targetUUID == null) {
                        source.sendMessage(mm.deserialize(lang.format("command-whitelist-player-not-found", Map.of("player-name", targetName))));
                        return;
                    }
                    plugin.getMySQLManager().removeFromWhitelist(targetUUID, groupId);
                    source.sendMessage(mm.deserialize(lang.format("command-whitelist-removed", Map.of("player", targetName, "group", groupName))));

                    logAction(operatorName, operatorUUID, "WHITELIST_REMOVE", targetName, groupName, "entfernt");
                    sendAdminLog("command-whitelist-removed-admin", Map.of("player-name", operatorName, "target", targetName, "group", groupName), source);
                });
            }
            case "list" -> {
                List<String> names = plugin.getMySQLManager().getWhitelistPlayers(groupId);
                source.sendMessage(mm.deserialize(lang.format("command-whitelist-list-header", Map.of("group", groupName))));
                source.sendMessage(mm.deserialize("<" + plugin.getConfig().getColorGrey() + ">" + String.join(", ", names)));
            }
        }
    }

    /**
     * Versucht die UUID eines Spielers zu finden.
     * Reihenfolge: Online-Spieler -> Eigene Datenbank -> Mojang API
     */
    private UUID fetchUUID(String playerName) {
        // 1. Ist der Spieler online?
        Optional<Player> online = server.getPlayer(playerName);
        if (online.isPresent()) return online.get().getUniqueId();

        // 2. In lokaler Datenbank suchen
        UUID cached = plugin.getMySQLManager().getUUIDByUsername(playerName);
        if (cached != null) return cached;

        // 3. Mojang API Fallback für komplett neue Spieler
        return getUUIDFromMojang(playerName);
    }

    private UUID getUUIDFromMojang(String playerName) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);

            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                String json = response.toString();
                String id = json.split("\"id\":\"")[1].split("\"")[0];

                return UUID.fromString(id.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void handleToggle(CommandSource source, String group, boolean active, String opName, UUID opUUID) {
        plugin.getMySQLManager().setWhitelistState(group, active);
        String stateText = active ? "AN" : "AUS";
        source.sendMessage(mm.deserialize(lang.format("command-whitelist-state", Map.of("group", group, "state", stateText))));

        logAction(opName, opUUID, "WHITELIST_STATE", group, group, active ? "aktiviert" : "deaktiviert");
        sendAdminLog("command-whitelist-state-admin", Map.of("player-name", opName, "group", group, "state", active ? "aktiviert" : "deaktiviert"), source);
    }

    private void logAction(String opName, UUID opUUID, String action, String target, String group, String verb) {
        String logText = "<" + config.getColorSecondary() + ">" + opName + "<" + config.getColorPrimary() + "> hat die Whitelist für " + group + " (" + target + ") " + verb;
        consoleLogger.broadCastToConsole(logText);
        plugin.getMySQLManager().logAction(opUUID, action, target, "Group: " + group);
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

        if (args.length <= 1) {
            return List.of("add", "remove", "list", "on", "off").stream().filter(s -> s.startsWith(currentArg)).toList();
        }

        if (args.length == 2) {
            List<String> groups = new ArrayList<>(plugin.getGroupManager().getAllGroupIdentifiers());
            groups.add("proxy");
            return groups.stream().filter(s -> s.startsWith(currentArg)).toList();
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            return server.getAllPlayers().stream().map(Player::getUsername).filter(s -> s.toLowerCase().startsWith(currentArg)).toList();
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.whitelist");
    }
}