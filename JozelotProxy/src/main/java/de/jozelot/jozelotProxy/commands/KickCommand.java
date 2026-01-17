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
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.*;

public class KickCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final ConfigManager config;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public KickCommand(JozelotProxy plugin) {
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

        if (!source.hasPermission("network.command.kick")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            return;
        }

        if (args.length < 2) {
            source.sendMessage(mm.deserialize(lang.format("command-kick-usage", null)));
            return;
        }

        Optional<Player> target = server.getPlayer(args[0]);
        if (target.isEmpty()) {
            source.sendMessage(mm.deserialize(lang.format("command-kick-player-not-online", Map.of("player-name", args[0]))));
            return;
        }

        Player targetPlayer = target.get();
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        LuckPerms lp = LuckPermsProvider.get();
        lp.getUserManager().loadUser(targetPlayer.getUniqueId()).thenAcceptAsync(user -> {
            int sourceWeight = getWeight(source, lp);
            int targetWeight = (user != null) ?
                    (user.getPrimaryGroup().equals("default") ? 0 : lp.getGroupManager().getGroup(user.getPrimaryGroup()).getWeight().orElse(0)) : 0;

            if (sourceWeight <= targetWeight && !(source instanceof ProxyServer)) {
                source.sendMessage(mm.deserialize(lang.format("command-kick-hierarchy-error", Map.of("player-name", args[0]))));
                return;
            }

            UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
            plugin.getMySQLManager().addPunishment(
                    targetPlayer.getUniqueId(),
                    operatorUUID,
                    "KICK",
                    "0s",
                    reason
            );

            List<String> kickLines = lang.formatList("kick-screen", Map.of("reason", reason));
            targetPlayer.disconnect(mm.deserialize(String.join("<newline>", kickLines)));

            source.sendMessage(mm.deserialize(lang.format("command-kick-success", Map.of("player-name", args[0], "reason", reason))));

            String senderName = (source instanceof Player p) ? p.getUsername() : "Konsole";
            consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + senderName +
                    "<" + config.getColorPrimary() + "> hat " + args[0] + " gekickt. Grund: " + reason);

            for (Player player : server.getAllPlayers()) {
                if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                    player.sendMessage(mm.deserialize(lang.format("command-kick-success-admin",
                            Map.of("player-name", senderName, "target-name", args[0], "reason", reason))));
                }
            }
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
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (!invocation.source().hasPermission("network.command.kick")) return List.of();

        if (args.length <= 1) {
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .sorted()
                    .toList();
        }
        return (args.length == 2) ? List.of("<reason>") : List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.kick");
    }
}