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

import java.util.*;

public class UnbanCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final ConfigManager config;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public UnbanCommand(JozelotProxy plugin) {
        this.server = plugin.getServer();
        this.lang = plugin.getLang();
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.consoleLogger = plugin.getConsoleLogger();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.unban")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            if (source instanceof Player) {
                String soundPath = lang.getRaw("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        source.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                        plugin.getConsoleLogger().broadCastToConsole("Fehlerhafter Sound-Key: '" + soundPath + "'");
                    }
                }
            }
            return;
        }

        if (args.length != 1) {
            source.sendMessage(mm.deserialize(lang.format("command-unban-usage", null)));
            if (source instanceof Player) {
                String soundPath = lang.getRaw("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        source.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                        plugin.getConsoleLogger().broadCastToConsole("Fehlerhafter Sound-Key: '" + soundPath + "'");
                    }
                }
            }
            return;
        }

        String targetName = args[0];

        UUID targetUUID = plugin.getMySQLManager().getUUIDFromName(targetName);

        if (targetUUID == null) {
            source.sendMessage(mm.deserialize(lang.format("command-ban-player-not-found", Map.of("player-name", targetName))));
            if (source instanceof Player) {
                String soundPath = lang.getRaw("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        source.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                        plugin.getConsoleLogger().broadCastToConsole("Fehlerhafter Sound-Key: '" + soundPath + "'");
                    }
                }
            }
            return;
        }

        boolean wasBanned = plugin.getMySQLManager().removePunishment(targetUUID, "BAN");

        if (wasBanned) {
            source.sendMessage(mm.deserialize(lang.format("command-unban-success", Map.of("player-name", targetName))));
            String name = (source instanceof Player player) ? player.getUsername() : "Konsole";

            consoleLogger.broadCastToConsole("<" + config.getColorSecondary() + ">" + name + "<" + config.getColorPrimary() + "> hat " + args[0] + " entbannt");
            for (Player player : server.getAllPlayers()) {
                if (player.hasPermission("network.get.logs") && !player.equals(source)) {
                    player.sendMessage(mm.deserialize(lang.format("command-unban-success-admin", Map.of("player-name", name, "ban-name", args[0]))));
                }
            }
            UUID operatorUUID = (source instanceof Player p) ? p.getUniqueId() : new UUID(0L, 0L);
            plugin.getMySQLManager().logAction(operatorUUID, "UNBAN", targetName, "");

            if (source instanceof Player) {
                String soundPath = lang.getRaw("sounds.success");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        source.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                        plugin.getConsoleLogger().broadCastToConsole("Fehlerhafter Sound-Key: '" + soundPath + "'");
                    }
                }
            }
        } else {
            source.sendMessage(mm.deserialize(lang.format("command-unban-not-banned", Map.of("player-name", targetName))));
            if (source instanceof Player) {
                String soundPath = lang.getRaw("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        source.playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                        plugin.getConsoleLogger().broadCastToConsole("Fehlerhafter Sound-Key: '" + soundPath + "'");
                    }
                }
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length <= 1) {
            String currentArg = args.length > 0 ? args[0].toLowerCase() : "";

            return plugin.getMySQLManager().getBannedPlayerNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .sorted()
                    .toList();
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.unban");
    }
}