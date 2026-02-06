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
import java.util.concurrent.TimeUnit;

public class PlayerInfoCommand implements SimpleCommand {

    private final LangManager lang;
    private final ProxyServer server;
    MiniMessage mm = MiniMessage.miniMessage();
    private final JozelotProxy plugin;

    public PlayerInfoCommand(JozelotProxy plugin) {
        this.lang = plugin.getLang();
        this.server = plugin.getServer();
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!source.hasPermission("network.command.playerinfo")) {
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

        if (args.length < 1) {
            source.sendMessage(mm.deserialize(lang.format("command-playerinfo-usage", null)));
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

        Optional<Player> target = server.getPlayer(args[0]);

        if (!target.isPresent()) {
            source.sendMessage(mm.deserialize(lang.format("command-kick-player-not-online", Map.of("player-name", args[0]))));
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

        Player targetFinal = target.get();

        String version = targetFinal.getProtocolVersion().getName();
        long ping = targetFinal.getPing();
        String ip = targetFinal.getRemoteAddress().getAddress().getHostAddress();

        int groupId = plugin.getGroupManager().getGroupId(targetFinal.getCurrentServer()
                .map(s -> s.getServerInfo().getName()).orElse(""));
        String groupName = (groupId != -1) ? plugin.getGroupManager().getGroupName(groupId) : "Keine Gruppe";

        Long loginMillis = plugin.getLoginTimes().get(targetFinal.getUniqueId());
        String onlineSince;

        if (loginMillis != null) {
            long diff = System.currentTimeMillis() - loginMillis;
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;

            if (hours > 0) {
                onlineSince = String.format("%dh %dm", hours, minutes);
            } else if (minutes > 0) {
                onlineSince = String.format("%dm %ds", minutes, seconds);
            } else {
                onlineSince = String.format("%ds", seconds);
            }
        } else {
            onlineSince = "{danger}Unbekannt";
        }

        List<String> altList = plugin.getMySQLManager().getAlts(ip, targetFinal.getUsername());

        String altsFormatted;
        if (altList.isEmpty()) {
            altsFormatted = lang.format("command-playerinfo-no-more-accounts", null);
        } else {
            altsFormatted = altList.stream()
                    .map(name -> "<click:run_command:'/playerinfo " + name + "'><hover:show_text:'" + lang.format("command-playerinfo-click-for-more", Map.of("name", name)) + "'><white>" + name + "</white></hover></click>")
                    .collect(java.util.stream.Collectors.joining("{grey}, "));
        }

        String clientBrand = targetFinal.getClientBrand() != null ? targetFinal.getClientBrand() : "Vanilla*";

        Map<String, String> placeholders = new java.util.HashMap<>();
        placeholders.put("player", targetFinal.getUsername());
        placeholders.put("uuid", targetFinal.getUniqueId().toString());
        placeholders.put("ping", String.valueOf(ping));
        placeholders.put("ip", ip);
        placeholders.put("version", version);
        placeholders.put("server", targetFinal.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("N/A"));
        placeholders.put("group", groupName);
        placeholders.put("online_time", "Session basiert");
        placeholders.put("alts", altsFormatted);
        placeholders.put("client", clientBrand);
        placeholders.put("online_time", onlineSince);

        source.sendMessage(mm.deserialize(String.join("<newline>", lang.formatList("command-playerinfo-sucess", placeholders))));
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
        return invocation.source().hasPermission("network.command.playerinfo");
    }
}

