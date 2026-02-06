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

public class BanListCommand implements SimpleCommand {

    private final JozelotProxy plugin;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public BanListCommand(JozelotProxy plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        if (!source.hasPermission("network.command.banlist")) {
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

        List<Map<String, String>> activeBans = plugin.getMySQLManager().getAllActiveBans();

        if (activeBans.isEmpty()) {
            source.sendMessage(mm.deserialize(lang.format("command-banlist-empty", null)));
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

        source.sendMessage(mm.deserialize(lang.format("command-banlist-header", Map.of("count", String.valueOf(activeBans.size())))));

        for (Map<String, String> ban : activeBans) {
            source.sendMessage(mm.deserialize(lang.format("command-banlist-entry", Map.of(
                    "target", ban.get("target"),
                    "operator", ban.get("operator"),
                    "reason", ban.get("reason"),
                    "duration", ban.get("duration")
            ))));
        }

        source.sendMessage(mm.deserialize(lang.format("command-banlist-footer", null)));
        if (source instanceof Player) {
            String soundPath = lang.getRaw("sounds.pling");
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

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.banlist");
    }
}