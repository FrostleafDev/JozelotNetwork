package de.jozelot.jozelotProxy.commands.messaging;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;

public class MsgCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final ConfigManager config;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MsgCommand(JozelotProxy plugin) {
        this.server = plugin.getServer();
        this.lang = plugin.getLang();
        this.plugin = plugin;
        this.consoleLogger = plugin.getConsoleLogger();
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("network.command.msg")) {
            invocation.source().sendMessage(mm.deserialize(lang.getNoPermission()));
            if (invocation.source() instanceof Player) {
                String soundPath = lang.getRaw("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        invocation.source().playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                        plugin.getConsoleLogger().broadCastToConsole("Fehlerhafter Sound-Key: '" + soundPath + "'");
                    }
                }
            }
            return;
        }

        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(mm.deserialize(lang.getOnlyPlayer()));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 2) {
            player.sendMessage(mm.deserialize(lang.format("command-msg-usage", Map.of())));
            if (invocation.source() instanceof Player) {
                String soundPath = lang.getRaw("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        invocation.source().playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                        plugin.getConsoleLogger().broadCastToConsole("Fehlerhafter Sound-Key: '" + soundPath + "'");
                    }
                }
            }
            return;
        }

        String targetName = args[0];
        Optional<Player> targetOptional = server.getPlayer(targetName);

        if (targetOptional.isEmpty() || !isSameGroup(player, targetOptional.get())) {
            player.sendMessage(mm.deserialize(lang.format("command-msg-not-in-group",
                    Map.of("player-name", targetName))));
            if (invocation.source() instanceof Player) {
                String soundPath = lang.getRaw("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        invocation.source().playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                        plugin.getConsoleLogger().broadCastToConsole("Fehlerhafter Sound-Key: '" + soundPath + "'");
                    }
                }
            }
            return;
        }

        Player target = targetOptional.get();
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(mm.deserialize(lang.format("command-gmsg-self-msg", Map.of())));
            if (invocation.source() instanceof Player) {
                String soundPath = lang.getRaw("sounds.error");
                if (!soundPath.isEmpty()) {
                    try {
                        Sound successSound = Sound.sound(
                                Key.key(soundPath),
                                Sound.Source.UI,
                                1.0f,
                                1.0f
                        );
                        invocation.source().playSound(successSound, Sound.Emitter.self());
                    } catch (Exception e) {
                        plugin.getConsoleLogger().broadCastToConsole("Fehlerhafter Sound-Key: '" + soundPath + "'");
                    }
                }
            }
            return;
        }

        String rawMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String processedMessage = player.hasPermission("network.chat.minimessage") ? rawMessage : mm.escapeTags(rawMessage);

        player.sendMessage(mm.deserialize(lang.format("msg-format-send", Map.of("target-name", target.getUsername(), "message", processedMessage))));
        target.sendMessage(mm.deserialize(lang.format("msg-format-receive", Map.of("player-name", player.getUsername(), "message", processedMessage))));

        if (target instanceof Player) {
            String soundPath = lang.getRaw("sounds.notify");
            if (!soundPath.isEmpty()) {
                try {
                    Sound successSound = Sound.sound(
                            Key.key(soundPath),
                            Sound.Source.UI,
                            1.0f,
                            1.0f
                    );
                    target.playSound(successSound, Sound.Emitter.self());
                } catch (Exception e) {
                    plugin.getConsoleLogger().broadCastToConsole("Fehlerhafter Sound-Key: '" + soundPath + "'");
                }
            }
        }

        plugin.getReplyMap().put(target.getUniqueId(), new JozelotProxy.ReplyData(player.getUniqueId(), false));
        consoleLogger.broadCastToConsole("[Local-MSG] " + player.getUsername() + " -> " + target.getUsername() + ": " + rawMessage);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) return List.of();

        String[] args = invocation.arguments();
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (args.length <= 1) {
            return server.getAllPlayers().stream()
                    .filter(target -> isSameGroup(player, target))
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .filter(name -> !name.equalsIgnoreCase(player.getUsername()))
                    .sorted()
                    .toList();
        }

        return List.of();
    }

    private boolean isSameGroup(Player p1, Player p2) {
        Optional<RegisteredServer> s1 = p1.getCurrentServer().map(p -> p.getServer());
        Optional<RegisteredServer> s2 = p2.getCurrentServer().map(p -> p.getServer());

        if (s1.isEmpty() || s2.isEmpty()) return false;

        String name1 = s1.get().getServerInfo().getName().split("-")[0].toLowerCase();
        String name2 = s2.get().getServerInfo().getName().split("-")[0].toLowerCase();

        return name1.equals(name2);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.msg");
    }
}