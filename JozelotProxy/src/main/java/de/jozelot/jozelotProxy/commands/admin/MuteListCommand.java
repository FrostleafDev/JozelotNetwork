package de.jozelot.jozelotProxy.commands.admin;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;

public class MuteListCommand implements SimpleCommand {

    private final JozelotProxy plugin;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MuteListCommand(JozelotProxy plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!source.hasPermission("network.command.mutelist")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            playSound(source, "error");
            return;
        }

        List<Map<String, String>> activeMutes = plugin.getMySQLManager().getAllActiveMutes();

        if (activeMutes.isEmpty()) {
            source.sendMessage(mm.deserialize(lang.format("command-mutelist-empty", null)));
            playSound(source, "error");
            return;
        }

        source.sendMessage(mm.deserialize(lang.format("command-mutelist-header", Map.of("count", String.valueOf(activeMutes.size())))));

        for (Map<String, String> mute : activeMutes) {
            source.sendMessage(mm.deserialize(lang.format("command-mutelist-entry", Map.of(
                    "target", mute.getOrDefault("target", "Unbekannt"),
                    "operator", mute.getOrDefault("operator", "Konsole"),
                    "reason", mute.getOrDefault("reason", "Kein Grund"),
                    "duration", mute.getOrDefault("duration", "Permanent")
            ))));
        }

        source.sendMessage(mm.deserialize(lang.format("command-mutelist-footer", null)));
        playSound(source, "pling");
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.mutelist");
    }

    private void playSound(CommandSource source, String soundKey) {
        if (!(source instanceof Player player)) return;
        String soundPath = lang.getRaw("sounds." + soundKey);
        if (soundPath == null || soundPath.isEmpty()) return;
        try {
            String cleanedPath = soundPath.trim().toLowerCase();
            if (!cleanedPath.contains(":")) cleanedPath = "minecraft:" + cleanedPath;
            player.playSound(Sound.sound(Key.key(cleanedPath), Sound.Source.UI, 1.0f, 1.0f), Sound.Emitter.self());
        } catch (Exception ignored) {}
    }
}