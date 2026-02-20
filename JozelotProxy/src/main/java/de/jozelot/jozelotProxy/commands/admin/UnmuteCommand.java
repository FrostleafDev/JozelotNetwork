package de.jozelot.jozelotProxy.commands.admin;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UnmuteCommand implements SimpleCommand {

    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ProxyServer server;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public UnmuteCommand(JozelotProxy plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.server = plugin.getServer();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.unmute")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            playSound(source, "error");
            return;
        }

        if (args.length != 1) {
            source.sendMessage(mm.deserialize(lang.format("command-unmute-usage", null)));
            playSound(source, "error");
            return;
        }

        UUID targetUUID = plugin.getMySQLManager().getUUIDFromName(args[0]);
        if (targetUUID == null) {
            source.sendMessage(mm.deserialize(lang.format("player-not-found", Map.of("player-name", args[0]))));
            playSound(source, "error");
            return;
        }

        boolean removedMute = plugin.getMySQLManager().removePunishment(targetUUID, "MUTE");
        boolean removedShadow = plugin.getMySQLManager().removePunishment(targetUUID, "SHADOWMUTE");

        if (removedMute || removedShadow) {
            source.sendMessage(mm.deserialize(lang.format("command-unmute-success", Map.of("player-name", args[0]))));
            playSound(source, "success");

            String opName = (source instanceof Player p) ? p.getUsername() : "Konsole";
            UUID opUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);

            String logMessage = lang.format("command-unmute-success-admin", Map.of(
                    "player-name", opName,
                    "mute-name", args[0]
            ));

            server.getAllPlayers().stream()
                    .filter(p -> p.hasPermission("network.get.logs"))
                    .filter(p -> !p.equals(source))
                    .forEach(p -> p.sendMessage(mm.deserialize(logMessage)));

            plugin.getMySQLManager().logAction(opUUID, "UNMUTE", args[0], "");

        } else {
            source.sendMessage(mm.deserialize(lang.format("command-unmute-not-muted", Map.of("player-name", args[0]))));
            playSound(source, "error");
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (!invocation.source().hasPermission("network.command.unmute")) {
            return List.of();
        }

        if (args.length <= 1) {
            String currentArg = args.length > 0 ? args[0].toLowerCase() : "";

            return plugin.getMySQLManager().getMutedPlayerNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .sorted()
                    .toList();
        }

        return List.of();
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

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.unmute");
    }
}