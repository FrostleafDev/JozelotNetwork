package de.jozelot.jozelotProxy.commands.admin;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import de.jozelot.jozelotProxy.utils.PlayerSends;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TpoHereCommand implements SimpleCommand {

    private final LangManager lang;
    private final ProxyServer server;
    private final PlayerSends playerSends;
    private MiniMessage mm = MiniMessage.miniMessage();
    private final JozelotProxy plugin;
    private final ConsoleLogger consoleLogger;

    public TpoHereCommand(JozelotProxy plugin) {
        this.lang = plugin.getLang();
        this.server = plugin.getServer();
        this.playerSends = plugin.getPlayerSends();
        this.plugin = plugin;
        this.consoleLogger = plugin.getConsoleLogger();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.tpo")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            playSound(source, "error");
            return;
        }

        if (!(source instanceof Player player)) {
            source.sendMessage(mm.deserialize(lang.getOnlyPlayer()));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(mm.deserialize(lang.format("command-tpohere-usage", null)));
            playSound(source, "error");
            return;
        }

        Optional<Player> target = server.getPlayer(args[0]);
        if (target.isEmpty()) {
            player.sendMessage(mm.deserialize(lang.format("command-ban-player-not-found", Map.of("player-name", args[0]))));
            playSound(source, "error");
            return;
        }

        boolean silent = false;
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("--silent") || args[1].equalsIgnoreCase("-s")) {
                silent = true;
            }
        }


        playerSends.sendPlayerToPlayer2(player, target.get(), silent);

        String senderName = (source instanceof Player) ? player.getUsername() : "Konsole";

        consoleLogger.broadCastToConsole("<yellow>" + senderName + " <gray>hat <white>" + args[0] + " <gray>zu sich <gray>verschoben");
        for (Player p : server.getAllPlayers()) {
            if (p.hasPermission("network.get.logs") && !p.equals(source)) {
                p.sendMessage(mm.deserialize(lang.format("command-tpohere-success-admin", Map.of("player-name", senderName, "target-name", target.get().getUsername()))));
            }
        }

        UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
        plugin.getMySQLManager().logAction(operatorUUID, "TPOHERE", args[0], "");

        playSound(source, "error");
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (args.length <= 1) {
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .toList();
        } else if (args.length == 2) {
            return List.of("--silent").stream()
                    .filter(s -> s.startsWith(currentArg))
                    .toList();
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.tpohere");
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
        } catch (Exception e) {
        }
    }
    // playSound(source, "error");
}
