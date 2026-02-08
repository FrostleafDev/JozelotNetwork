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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FindCommand implements SimpleCommand {

    private final LangManager lang;
    private final ProxyServer server;
    MiniMessage mm = MiniMessage.miniMessage();
    private final JozelotProxy plugin;

    public FindCommand(JozelotProxy plugin) {
        this.lang = plugin.getLang();
        this.server = plugin.getServer();
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.find")) {
            source.sendMessage(mm.deserialize(lang.getNoPermission()));
            playSound(source, "error");
            return;
        }
        if (args.length == 0) {
            source.sendMessage(mm.deserialize(lang.format("command-find-missing-argument", null)));
            playSound(source, "error");
            return;
        }

        String targetName = args[0];

        Optional<Player> onlineTarget = server.getPlayer(targetName);

        plugin.getServer().getScheduler().buildTask(plugin, () -> {
            Map<String, String> data = plugin.getMySQLManager().getOfflinePlayerInfo(targetName);

            if (data == null) {
                source.sendMessage(mm.deserialize(lang.format("command-find-not-found", Map.of("player-name", targetName))));
                playSound(source, "error");
                return;
            }

            String playerName = data.get("name");
            String serverDisplayName = data.get("server");

            if (onlineTarget.isPresent()) {
                onlineTarget.get().getCurrentServer().ifPresentOrElse(connection -> {
                    source.sendMessage(mm.deserialize(lang.format("command-find-success",
                            Map.of("player-name", playerName, "server-name", serverDisplayName))));
                    playSound(source, "pling");
                }, () -> {
                    source.sendMessage(mm.deserialize(lang.format("command-find-connecting", Map.of("player-name", playerName))));
                    playSound(source, "error");
                });
                return;
            }

            source.sendMessage(mm.deserialize(lang.format("command-find-offline",
                    Map.of("player-name", playerName, "server-name", serverDisplayName))));
            playSound(source, "pling");

        }).schedule();
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length > 1) return List.of();

        String input = args.length == 1 ? args[0].toLowerCase() : "";
        List<String> suggestions = new ArrayList<>();

        server.getAllPlayers().stream()
                .map(Player::getUsername)
                .filter(name -> name.toLowerCase().startsWith(input))
                .forEach(suggestions::add);

        return suggestions;
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("network.command.find");
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
            plugin.getConsoleLogger().broadCastToConsole("§cUngültiger Sound-Key in lang.yml: '" + soundPath + "'");
        }
    }
    // playSound(source, "error");
}
