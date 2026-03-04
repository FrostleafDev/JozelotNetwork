package de.jozelot.jozelotLobby.commands;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.secrets.objects.Secret;
import de.jozelot.jozelotLobby.secrets.objects.SecretRegion;
import de.jozelot.jozelotLobby.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecretCommand implements CommandExecutor, TabCompleter {

    private final JozelotLobby plugin;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public SecretCommand(JozelotLobby plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(mm.deserialize(lang.format("only-player", null)));
            return true;
        }

        if (!player.hasPermission("network.lobby.command.secret")) {
            player.sendMessage(mm.deserialize(lang.format("no-permission", null)));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.getSecretMgr().reload();
                player.sendMessage(mm.deserialize(lang.format("secret.reload-success", null)));
            }
            case "list" -> {
                player.sendMessage(mm.deserialize(lang.format("secret.list-header", null)));
                for (Secret s : plugin.getSecretMgr().getSecrets()) {
                    String worlds = s.getRegions().isEmpty() ? "Keine Region" :
                            s.getRegions().stream()
                                    .map(SecretRegion::getWorldName)
                                    .distinct()
                                    .sorted()
                                    .reduce((a, b) -> a + ", " + b)
                                    .orElse("Unbekannt");

                    player.sendMessage(mm.deserialize(lang.format("secret.list-item", Map.of(
                            "id", String.valueOf(s.getId()),
                            "name", s.getName(),
                            "worlds", worlds
                    ))));
                }
            }
            case "remove" -> {
                if (args.length < 2) {
                    sendHelp(player);
                    return true;
                }
                try {
                    int id = Integer.parseInt(args[1]);
                    plugin.getSecretMgr().removeSecret(id);
                    player.sendMessage(mm.deserialize(lang.format("secret.remove-success", Map.of("id", String.valueOf(id)))));
                } catch (NumberFormatException e) {
                    player.sendMessage(mm.deserialize(lang.format("secret.invalid-id", Map.of("input", args[1]))));
                }
            }
            case "add" -> {
                List<String> parsed = parseArguments(args);
                if (parsed.size() < 4) {
                    sendHelp(player);
                    return true;
                }
                Location[] selection = plugin.getSecretMgr().getWorldEditSelection(player);
                if (selection == null) {
                    player.sendMessage(mm.deserialize(lang.format("secret.add-pos-missing", null)));
                    return true;
                }

                int resultId = plugin.getSecretMgr().addSecret(parsed.get(1), parsed.get(3), parsed.get(2).toUpperCase(), selection[0], selection[1]);

                if (resultId != -1) {
                    player.sendMessage(mm.deserialize(lang.format("secret.add-success", Map.of("name", parsed.get(1)))));
                } else {
                    player.sendMessage(mm.deserialize("<red>Fehler: Das Secret konnte nicht in der Datenbank gespeichert werden. Prüfe die Konsole."));
                }
            }
            case "edit" -> handleEditCommand(player, args);
            case "player" -> handlePlayerCommand(player, args);
            default -> sendHelp(player);
        }
        return true;
    }

    private void handleEditCommand(Player player, String[] args) {
        List<String> parsed = parseArguments(args);
        if (parsed.size() < 3) {
            sendHelp(player);
            return;
        }
        try {
            int id = Integer.parseInt(parsed.get(1));
            String subAction = parsed.get(2).toLowerCase();
            if (parsed.size() >= 4) {
                String val = parsed.get(3);
                switch (subAction) {
                    case "name" -> plugin.getSecretDb().updateSecretValue(id, "name", val);
                    case "description", "desc" -> plugin.getSecretDb().updateSecretValue(id, "description", val);
                    case "material", "item", "block" -> plugin.getSecretDb().updateSecretValue(id, "block", val.toUpperCase());
                }
                player.sendMessage(mm.deserialize(lang.format("secret.edit-success", Map.of("id", String.valueOf(id)))));
            } else if (subAction.contains("region")) {
                Location[] sel = plugin.getSecretMgr().getWorldEditSelection(player);
                if (sel == null) {
                    player.sendMessage(mm.deserialize(lang.format("secret.add-pos-missing", null)));
                    return;
                }
                SecretRegion region = new SecretRegion(sel[0].toVector(), sel[1].toVector(), sel[0].getWorld().getName());
                if (subAction.equals("setregion")) plugin.getSecretDb().removeRegions(id);
                plugin.getSecretDb().addRegionToSecret(id, region);
                player.sendMessage(mm.deserialize(lang.format("secret.edit-region-added", Map.of("id", String.valueOf(id)))));
            }
            plugin.getSecretMgr().reload();
        } catch (NumberFormatException e) {
            player.sendMessage(mm.deserialize(lang.format("secret.invalid-id", Map.of("input", parsed.get(1)))));
        }
    }

    private List<String> parseArguments(String[] args) {
        List<String> list = new ArrayList<>();
        String joined = String.join(" ", args);
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(joined);
        while (m.find()) {
            list.add(m.group(1).replace("\"", ""));
        }
        return list;
    }

    private void handlePlayerCommand(Player admin, String[] args) {
        if (args.length < 3) return;
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            admin.sendMessage(mm.deserialize(lang.format("player-not-found", Map.of("player-name", args[2]))));
            return;
        }
        LobbyPlayer lp = plugin.getLobbyPlayerManager().getPlayer(target);
        if (lp == null) return;
        String action = args[1].toLowerCase();
        if (action.equals("list")) {
            admin.sendMessage(mm.deserialize(lang.format("secret.player-list-header", Map.of("player", target.getName()))));
            lp.getFoundSecretIds().forEach(id -> admin.sendMessage(mm.deserialize(lang.format("secret.player-list-item", Map.of("id", String.valueOf(id))))));
        } else if (args.length >= 4) {
            try {
                int sid = Integer.parseInt(args[3]);
                if (action.equals("add")) lp.addFoundSecret(sid);
                else lp.getFoundSecretIds().remove(sid);
                admin.sendMessage(mm.deserialize(lang.format("secret.player-modify-success", Map.of("id", String.valueOf(sid), "player", target.getName()))));
            } catch (NumberFormatException ignored) {}
        }
    }

    private void sendHelp(Player player) {
        List.of("header", "add", "remove", "list", "reload", "player-modify", "player-list", "edit")
                .forEach(key -> player.sendMessage(mm.deserialize(lang.format("secret.help." + key, null))));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("network.lobby.command.secret")) return List.of();

        List<String> suggestions = new ArrayList<>();
        String fullInput = String.join(" ", args);
        List<String> parsed = parseArguments(args);

        boolean endsWithSpace = fullInput.endsWith(" ");
        boolean inQuotes = (fullInput.length() - fullInput.replace("\"", "").length()) % 2 != 0;

        if (args.length == 1) {
            suggestions.addAll(List.of("add", "remove", "list", "player", "reload", "edit"));
        } else {
            String action = args[0].toLowerCase();
            int logicalIndex = parsed.size() + (endsWithSpace ? 1 : 0);

            switch (action) {
                case "add" -> {
                    if (logicalIndex == 2 && !inQuotes) suggestions.add("\"Name\"");
                    else if (logicalIndex == 3 && !inQuotes) {
                        for (Material m : Material.values()) {
                            if (!m.isLegacy() && m != Material.AIR) suggestions.add(m.name().toLowerCase());
                        }
                    }
                    else if (logicalIndex == 4 && !inQuotes) suggestions.add("\"Beschreibung\"");
                }
                case "edit" -> {
                    if (logicalIndex == 2) {
                        plugin.getSecretMgr().getSecrets().forEach(s -> suggestions.add(String.valueOf(s.getId())));
                    } else if (logicalIndex == 3) {
                        suggestions.addAll(List.of("name", "description", "material", "addregion", "setregion"));
                    } else if (logicalIndex == 4 && !inQuotes) {
                        String type = parsed.get(2).toLowerCase();
                        if (List.of("material", "item", "block").contains(type)) {
                            for (Material m : Material.values()) {
                                if (!m.isLegacy() && m != Material.AIR) suggestions.add(m.name().toLowerCase());
                            }
                        } else if (List.of("name", "description", "desc").contains(type)) {
                            suggestions.add("\"Wert\"");
                        }
                    }
                }
                case "player" -> {
                    if (logicalIndex == 2) suggestions.addAll(List.of("add", "remove", "list"));
                    else if (logicalIndex == 3) Bukkit.getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
                    else if (logicalIndex == 4) plugin.getSecretMgr().getSecrets().forEach(s -> suggestions.add(String.valueOf(s.getId())));
                }
                case "remove" -> {
                    if (logicalIndex == 2) plugin.getSecretMgr().getSecrets().forEach(s -> suggestions.add(String.valueOf(s.getId())));
                }
            }
        }

        String currentArg = args[args.length - 1].toLowerCase();
        return suggestions.stream()
                .filter(s -> s.toLowerCase().startsWith(currentArg))
                .sorted()
                .toList();
    }
}