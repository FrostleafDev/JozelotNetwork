package de.jozelot.jozelotProxy.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.*;

public class BanCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final ConfigManager config;
    private MiniMessage mm = MiniMessage.miniMessage();

    public BanCommand(JozelotProxy plugin) {
        this.server = plugin.getServer();
        this.lang = plugin.getLang();
        this.plugin = plugin;
        this.consoleLogger = plugin.getConsoleLogger();
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.ban")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        if (args.length < 3) {
            source.sendMessage(mm.deserialize(lang.format("command-ban-usage", null)));
            return;
        }

        // 1. UUID finden
        Optional<Player> onlineTarget = server.getPlayer(args[0]);
        UUID targetUUID = onlineTarget.map(Player::getUniqueId)
                .orElseGet(() -> plugin.getMySQLManager().getUUIDFromName(args[0]));

        if (targetUUID == null) {
            source.sendMessage(mm.deserialize(lang.format("command-ban-player-not-found", Map.of("player-name", args[0]))));
            return;
        }

        checkWeightAndExecute(source, targetUUID, args, onlineTarget.orElse(null));
    }

    private void checkWeightAndExecute(CommandSource source, UUID targetUUID, String[] args, Player onlineTarget) {
        LuckPerms lp = LuckPermsProvider.get();

        lp.getUserManager().loadUser(targetUUID).thenAcceptAsync(user -> {
            int sourceWeight = getWeight(source, lp);
            int targetWeight = (user != null) ? user.getPrimaryGroup().equals("default") ? 0 :
                    lp.getGroupManager().getGroup(user.getPrimaryGroup()).getWeight().orElse(0) : 0;

            if (sourceWeight <= targetWeight && !(source instanceof ProxyServer)) {
                source.sendMessage(mm.deserialize(lang.format("command-ban-hierarchy-error", Map.of("player-name", args[0]))));
                return;
            }

            String timeArgs = args[1].toLowerCase();
            if (!timeArgs.matches("\\d+[smhdwy]|mo") && !timeArgs.equals("permanent") && !timeArgs.equals("perma")) {
                source.sendMessage(mm.deserialize(lang.format("command-ban-invalid-time", Map.of("input", timeArgs))));
                return;
            }

            String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);

            // 4. Datenbank & Kick
            boolean alreadyBanned = plugin.getMySQLManager().addPunishment(targetUUID, operatorUUID, "BAN", timeArgs, reason);

            if (alreadyBanned) {
                source.sendMessage(mm.deserialize(lang.format("command-ban-already-banned", Map.of("player-name", args[0]))));
                return;
            }

            source.sendMessage(mm.deserialize(lang.format("command-ban-success", Map.of("player-name", args[0], "reason", reason))));
            String name = (source instanceof Player player) ? player.getUsername() : "Konsole";

            consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + name + "<" + config.getColorPrimary() + "> hat " + args[0] + " gebannt. Länge: " + timeArgs  + " - " + " Grund: " + reason);
            for (Player player : server.getAllPlayers()) {
                if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                    player.sendMessage(mm.deserialize(lang.format("command-ban-success-admin", Map.of("player-name", name, "ban-name", args[0], "duration", timeArgs))));
                }
            }

            if (onlineTarget != null) {
                List<String> kickLines = lang.formatList("ban-kick-screen", Map.of("reason", reason, "duration", timeArgs));
                onlineTarget.disconnect(mm.deserialize(String.join("<newline>", kickLines)));
            }

            plugin.getMySQLManager().logAction(operatorUUID, "BAN", args[0], "Grund: " + reason + " | Dauer: " + timeArgs);
        });
    }

    private int getWeight(CommandSource source, LuckPerms lp) {
        if (!(source instanceof Player player)) return Integer.MAX_VALUE;
        User user = lp.getUserManager().getUser(player.getUniqueId());
        if (user == null) return 0;
        return lp.getGroupManager().getGroup(user.getPrimaryGroup()).getWeight().orElse(0);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        List<String> list = new ArrayList<>();

        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (!invocation.source().hasPermission("network.command.ban")) return List.of();

        if (args.length <= 1) {
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .sorted()
                    .toList();
        }

        // 2. ARGUMENT: Zeit
        if (args.length == 2) {
            List<String> times = List.of("1m", "1h", "1d", "1w", "1mo", "1y", "permanent");
            return times.stream()
                    .filter(time -> time.toLowerCase().startsWith(currentArg))
                    .toList();
        }

        if (args.length == 3) {
            return config.getStringList("punishment-reasons").stream()
                    .filter(reason -> reason.toLowerCase().startsWith(currentArg))
                    .toList();
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.ban");
    }
}
