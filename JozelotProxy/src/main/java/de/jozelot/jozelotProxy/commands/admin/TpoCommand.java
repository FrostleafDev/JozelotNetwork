package de.jozelot.jozelotProxy.commands.admin;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.LangManager;
import de.jozelot.jozelotProxy.utils.PlayerSends;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TpoCommand implements SimpleCommand {

    private final LangManager lang;
    private final ProxyServer server;
    private final PlayerSends playerSends;
    private MiniMessage mm = MiniMessage.miniMessage();

    public TpoCommand(JozelotProxy plugin) {
        this.lang = plugin.getLang();
        this.server = plugin.getServer();
        this.playerSends = plugin.getPlayerSends();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.tpo")) {
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

                    }
                }
            }
            return;
        }

        if (!(source instanceof Player player)) {
            source.sendMessage(mm.deserialize(lang.getOnlyPlayer()));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(mm.deserialize(lang.format("command-tpo-usage", null)));
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
                    }
                }
            }
            return;
        }

        Optional<Player> target = server.getPlayer(args[0]);

        if (!target.isPresent()) {
            player.sendMessage(mm.deserialize(lang.format("command-tpo-player-not-found", Map.of("player-name", args[0]))));
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
                    }
                }
            }
            return;
        }

        Player targetFinal = target.get();

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
                }
            }
        }

        playerSends.sendPlayerToPlayer(player, targetFinal);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        String currentArg = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

        if (!invocation.source().hasPermission("network.command.tpo")) return List.of();

        if (args.length <= 1) {
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(currentArg))
                    .sorted()
                    .toList();
        }
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("network.command.tpo");
    }
}
