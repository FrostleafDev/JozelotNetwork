package de.jozelot.jozelotProxy.commands.messaging;

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
import java.util.Optional;

public class ReplyCommand implements SimpleCommand {

    private final ProxyServer server;
    private final JozelotProxy plugin;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ReplyCommand(JozelotProxy plugin) {
        this.server = plugin.getServer();
        this.plugin = plugin;
        this.lang = plugin.getLang();
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("network.command.reply")) {
            invocation.source().sendMessage(mm.deserialize(lang.getNoPermission()));
            playSound(invocation.source(), "error");
            return;
        }

        if (!(invocation.source() instanceof Player player)) {
            invocation.source().sendMessage(mm.deserialize(lang.getOnlyPlayer()));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 1) {
            player.sendMessage(mm.deserialize(lang.format("command-reply-usage", Map.of())));
            playSound(invocation.source(), "error");
            return;
        }

        JozelotProxy.ReplyData data = plugin.getReplyMap().get(player.getUniqueId());
        if (data == null) {
            player.sendMessage(mm.deserialize(lang.format("command-reply-no-target", Map.of())));
            playSound(invocation.source(), "error");
            return;
        }

        Optional<Player> targetOptional = server.getPlayer(data.partnerId());
        if (targetOptional.isEmpty()) {
            player.sendMessage(mm.deserialize(lang.format("command-reply-target-offline", Map.of())));
            plugin.getReplyMap().remove(player.getUniqueId());
            playSound(invocation.source(), "error");
            return;
        }

        Player target = targetOptional.get();

        String rawMessage = String.join(" ", args);
        String processedMessage = player.hasPermission("network.chat.minimessage") ? rawMessage : mm.escapeTags(rawMessage);

        String sendKey = data.isGlobal() ? "gmsg-format-send" : "msg-format-send";
        String receiveKey = data.isGlobal() ? "gmsg-format-receive" : "msg-format-receive";

        player.sendMessage(mm.deserialize(lang.format(sendKey, Map.of(
                "target-name", target.getUsername(),
                "message", processedMessage
        ))));

        target.sendMessage(mm.deserialize(lang.format(receiveKey, Map.of(
                "player-name", player.getUsername(),
                "message", processedMessage
        ))));

        playSound(target, "notify");
        sendSpyMessage(player, target, rawMessage, false);

        plugin.getReplyMap().put(target.getUniqueId(), new JozelotProxy.ReplyData(player.getUniqueId(), data.isGlobal()));

        plugin.getConsoleLogger().broadCastToConsole("[Reply] " + player.getUsername() + " -> " + target.getUsername() + ": " + rawMessage);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.reply");
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