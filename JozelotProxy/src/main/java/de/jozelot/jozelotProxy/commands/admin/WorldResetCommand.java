package de.jozelot.jozelotProxy.commands.admin;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class WorldResetCommand implements SimpleCommand {

    private final JozelotProxy plugin;
    private final ProxyServer server;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public WorldResetCommand(JozelotProxy plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.lang = plugin.getLang();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.world-reset")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        if (args.length < 1) {
            source.sendMessage(mm.deserialize(lang.format("command-worldreset-usage", null)));
            return;
        }

        String serverName = args[0].toLowerCase();

        if (!serverName.matches("challenge-[1-3]")) {
            source.sendMessage(mm.deserialize(lang.format("command-worldreset-usage", null)));
            return;
        }

        String pteroId = plugin.getMySQLManager().getPteroIdentifier(serverName);
        if (pteroId == null) {
            source.sendMessage(mm.deserialize(lang.format("pterodactyl-control-no-id", Map.of("server-name", serverName))));
            return;
        }

        server.getScheduler().buildTask(plugin, () -> {
            String senderName = (source instanceof Player p) ? p.getUsername() : "Konsole";

            source.sendMessage(mm.deserialize(lang.format("command-worldreset-started", Map.of("server-name", serverName))));

            source.sendMessage(mm.deserialize(lang.format("command-worldreset-stopping", null)));
            plugin.getPteroManager().sendAction(pteroId, "stop", code -> {});

            if (!waitForStatus(pteroId, "offline")) {
                source.sendMessage(mm.deserialize("{danger}Fehler: Server konnte nicht gestoppt werden. Abbruch."));
                return;
            }

            source.sendMessage(mm.deserialize(lang.format("command-worldreset-deleting", null)));
            plugin.getPteroManager().deleteFiles(pteroId, List.of("world", "world_nether", "world_the_end"));

            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            source.sendMessage(mm.deserialize(lang.format("command-worldreset-starting", null)));
            plugin.getPteroManager().sendAction(pteroId, "start", code -> {
                if (code == 204) {
                    source.sendMessage(mm.deserialize(lang.format("command-worldreset-complete", Map.of("server-name", serverName))));

                    UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
                    plugin.getMySQLManager().logAction(operatorUUID, "WORLD_RESET", "server:" + serverName, "Reset erfolgreich");

                    for (Player admin : server.getAllPlayers()) {
                        if (admin.hasPermission("network.get.logs") && !admin.equals(source)) {
                            admin.sendMessage(mm.deserialize(lang.format("world-reset-admin", Map.of("player-name", senderName, "server-name", serverName))));
                        }
                    }
                }
            });
        }).schedule();
    }

    private boolean waitForStatus(String pteroId, String targetStatus) {
        for (int i = 0; i < 30; i++) {
            try {
                var statusContainer = new Object() { String current = ""; };
                plugin.getPteroManager().getResources(pteroId, data -> {
                    if (data != null) statusContainer.current = data.get("current_state").getAsString();
                });

                Thread.sleep(1000);
                if (statusContainer.current.equalsIgnoreCase(targetStatus)) return true;
            } catch (InterruptedException ignored) {}
        }
        return false;
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length <= 1) {
            String input = args.length == 1 ? args[0].toLowerCase() : "";
            return List.of("challenge-1", "challenge-2", "challenge-3").stream()
                    .filter(s -> s.startsWith(input)).toList();
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.world-reset");
    }
}