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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
            playSound(source, "error");
            return;
        }

        if (args.length < 1) {
            source.sendMessage(mm.deserialize(lang.format("command-playerinfo-usage", null)));
            playSound(source, "error");
            return;
        }

        Optional<Player> target = server.getPlayer(args[0]);

        if (!target.isPresent()) {
            source.sendMessage(mm.deserialize(lang.format("command-kick-player-not-online", Map.of("player-name", args[0]))));
            playSound(source, "error");
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
                    .collect(Collectors.joining("{grey}, "));
        }

        String clientBrand = targetFinal.getClientBrand() != null ? targetFinal.getClientBrand() : "Vanilla*";

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", targetFinal.getUsername());
        placeholders.put("uuid", targetFinal.getUniqueId().toString());
        placeholders.put("ping", String.valueOf(ping));
        placeholders.put("ip", ip);
        placeholders.put("version", version);
        placeholders.put("server", targetFinal.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("N/A"));
        placeholders.put("group", groupName);
        placeholders.put("alts", altsFormatted);
        placeholders.put("client", clientBrand);
        placeholders.put("online_time", onlineSince);

        source.sendMessage(mm.deserialize(String.join("<newline>", lang.formatList("command-playerinfo-sucess", placeholders))));
        playSound(source, "pling");
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

