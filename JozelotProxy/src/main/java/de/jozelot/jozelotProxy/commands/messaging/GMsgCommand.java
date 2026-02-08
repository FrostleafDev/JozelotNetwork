package de.jozelot.jozelotProxy.commands.messaging;

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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GMsgCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final ConsoleLogger consoleLogger;
    private final ConfigManager config;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public GMsgCommand(JozelotProxy plugin) {
        this.server = plugin.getServer();
        this.lang = plugin.getLang();
        this.plugin = plugin;
        this.consoleLogger = plugin.getConsoleLogger();
        this.config = plugin.getConfig();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("network.command.gmsg")) {
            invocation.source().sendMessage(mm.deserialize(lang.getNoPermission()));
            playSound(invocation.source(), "error");
            return;
        }

        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(mm.deserialize(lang.getOnlyPlayer()));
            return;
        }

        String[] args = invocation.arguments();

        if (args.length < 2) {
            player.sendMessage(mm.deserialize(lang.format("command-gmsg-usage", Map.of())));
            playSound(invocation.source(), "error");
            return;
        }

        String targetName = args[0];
        Optional<Player> targetOptional = server.getPlayer(targetName);

        if (targetOptional.isEmpty()) {
            player.sendMessage(mm.deserialize(lang.format("command-gmsg-target-not-found",
                    Map.of("player-name", targetName))));
            playSound(invocation.source(), "error");
            return;
        }

        Player target = targetOptional.get();

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(mm.deserialize(lang.format("command-gmsg-self-msg", Map.of())));
            playSound(invocation.source(), "error");
            return;
        }

        String rawMessage = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        String processedMessage = rawMessage;
        if (!player.hasPermission("network.chat.minimessage")) {
            processedMessage = mm.escapeTags(rawMessage);
        }

        String sendFormat = lang.format("gmsg-format-send", Map.of(
                "target-name", target.getUsername(),
                "message", processedMessage
        ));

        String receiveFormat = lang.format("gmsg-format-receive", Map.of(
                "player-name", player.getUsername(),
                "message", processedMessage
        ));

        player.sendMessage(mm.deserialize(sendFormat));
        target.sendMessage(mm.deserialize(receiveFormat));
        playSound(target, "notify");

        sendSpyMessage(player, target, rawMessage, true);

        plugin.getReplyMap().put(target.getUniqueId(), new JozelotProxy.ReplyData(player.getUniqueId(), true));
        consoleLogger.broadCastToConsole("[MSG] " + player.getUsername() + " -> " + target.getUsername() + ": " + rawMessage);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (!invocation.source().hasPermission("network.command.gmsg")) return List.of();

        if (args.length <= 1) {
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .filter(name -> !name.equalsIgnoreCase(((Player) invocation.source()).getUsername()))
                    .sorted()
                    .toList();
        }

        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.gmsg");
    }

    private void sendSpyMessage(Player sender, Player receiver, String message, boolean global) {
        String langKey = global ? "spy-gmsg" : "spy-msg";

        String spyFormat = lang.format(langKey, Map.of(
                "sender", sender.getUsername(),
                "receiver", receiver.getUsername(),
                "message", message
        ));

        for (Player admin : plugin.getServer().getAllPlayers()) {
            if (plugin.getSpyPlayers().contains(admin.getUniqueId())) {
                if (!admin.getUniqueId().equals(sender.getUniqueId()) && !admin.getUniqueId().equals(receiver.getUniqueId())) {
                    admin.sendMessage(mm.deserialize(spyFormat));
                }
            }
        }
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