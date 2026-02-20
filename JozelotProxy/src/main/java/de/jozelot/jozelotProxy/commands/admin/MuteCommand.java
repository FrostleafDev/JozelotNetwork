package de.jozelot.jozelotProxy.commands.admin;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.*;

public class MuteCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final ConfigManager config;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MuteCommand(JozelotProxy plugin) {
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
        String alias = invocation.alias();

        if (!source.hasPermission("network.command.mute")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        if (args.length < 3) {
            source.sendMessage(mm.deserialize(lang.format("command-mute-usage", null)));
            return;
        }

        boolean isShadow = alias.equalsIgnoreCase("shadowmute");

        Optional<Player> onlineTarget = server.getPlayer(args[0]);
        UUID targetUUID = onlineTarget.map(Player::getUniqueId)
                .orElseGet(() -> plugin.getMySQLManager().getUUIDFromName(args[0]));

        if (targetUUID == null) {
            source.sendMessage(mm.deserialize(lang.format("command-ban-player-not-found", Map.of("player-name", args[0]))));
            playSound(source, "error");
            return;
        }

        LuckPerms lp = LuckPermsProvider.get();
        lp.getUserManager().loadUser(targetUUID).thenAcceptAsync(user -> {
            int sourceWeight = getWeight(source, lp);
            int targetWeight = (user != null) ? lp.getGroupManager().getGroup(user.getPrimaryGroup()).getWeight().orElse(0) : 0;

            if (sourceWeight <= targetWeight && !(source instanceof ProxyServer)) {
                source.sendMessage(mm.deserialize(lang.format("command-mute-hierarchy-error", Map.of("player-name", args[0]))));
                playSound(source, "error");
                return;
            }

            String timeArgs = args[1].toLowerCase();
            String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            String type = isShadow ? "SHADOWMUTE" : "MUTE";
            UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
            String opName = (source instanceof Player p) ? p.getUsername() : "Konsole";

            boolean alreadyMuted = plugin.getMySQLManager().addPunishment(targetUUID, operatorUUID, type, timeArgs, reason);

            if (alreadyMuted) {
                source.sendMessage(mm.deserialize(lang.format("command-mute-already-muted", Map.of("player-name", args[0]))));
                playSound(source, "error");
                return;
            }

            source.sendMessage(mm.deserialize(lang.format("command-mute-success", Map.of(
                    "player-name", args[0],
                    "reason", reason,
                    "type", type,
                    "duration", timeArgs))));

            playSound(source, "success");

            String logMessage = lang.format("command-mute-success-admin", Map.of(
                    "player-name", opName,
                    "mute-name", args[0],
                    "reason", reason,
                    "duration", timeArgs
            ));

            if (isShadow) {
                logMessage += " <grey>(SHADOW)";
            }

            String finalLog = logMessage;
            server.getAllPlayers().stream()
                    .filter(p -> p.hasPermission("network.get.logs"))
                    .filter(p -> !p.equals(source))
                    .forEach(p -> p.sendMessage(mm.deserialize(finalLog)));

            consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + opName + "<" + config.getColorPrimary() + "> hat " + args[0] + " gemutet (" + type + "). Dauer: " + timeArgs);
            plugin.getMySQLManager().logAction(operatorUUID, type, args[0], "Grund: " + reason);

            if (onlineTarget.isPresent() && !isShadow) {
                onlineTarget.get().sendMessage(mm.deserialize(String.join("<newline>",
                        lang.formatList("mute-notification", Map.of("reason", reason, "duration", timeArgs)))));
            }
        });
    }

    private void playSound(CommandSource source, String soundKey) {
        if (!(source instanceof Player player)) return;

        String soundPath = lang.getRaw("sounds." + soundKey);
        if (soundPath == null || soundPath.isEmpty()) return;

        try {
            String cleanedPath = soundPath.trim().toLowerCase();
            if (!cleanedPath.contains(":")) {
                cleanedPath = "minecraft:" + cleanedPath;
            }

            Sound sound = Sound.sound(
                    Key.key(cleanedPath),
                    Sound.Source.UI,
                    1.0f,
                    1.0f
            );
            player.playSound(sound, Sound.Emitter.self());
        } catch (Exception ignored) {
        }
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
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (!invocation.source().hasPermission("network.command.mute")) return List.of();

        if (args.length <= 1) {
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .sorted().toList();
        }

        if (args.length == 2) {
            return List.of("1m", "1h", "1d", "1w", "1mo", "1y", "permanent").stream()
                    .filter(time -> time.startsWith(currentArg)).toList();
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
        return invocation.source().hasPermission("network.command.mute");
    }
}