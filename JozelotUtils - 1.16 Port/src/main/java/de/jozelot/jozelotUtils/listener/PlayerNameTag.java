package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerNameTag implements Listener {

    private final ConfigManager config;
    private final JozelotUtils plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public PlayerNameTag(JozelotUtils plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();

        LuckPerms api = LuckPermsProvider.get();
        api.getEventBus().subscribe(plugin, net.luckperms.api.event.user.UserDataRecalculateEvent.class, e -> {
            Player player = Bukkit.getPlayer(e.getUser().getUniqueId());
            if (player != null && player.isOnline()) {
                Bukkit.getScheduler().runTask(plugin, () -> updateNametag(player));
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player all : Bukkit.getOnlinePlayers()) {
                updateNametag(all);
            }
        }, 2L);
    }

    public void updateNametag(Player player) {
        if (!config.isShowPlayerNameTags()) return;

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        String rawPrefix = user.getCachedData().getMetaData().getPrefix();
        if (rawPrefix == null) rawPrefix = "";

        int weight = 0;
        if (api.getGroupManager().getGroup(user.getPrimaryGroup()) != null) {
            weight = api.getGroupManager().getGroup(user.getPrimaryGroup()).getWeight().orElse(0);
        }

        // Teamname für die Sortierung (000 bis 999)
        String teamName = String.format("%03d_%s", (999 - weight), user.getPrimaryGroup());
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);

        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }

        // 1. Präfix umwandeln
        Component prefixComponent = mm.deserialize(rawPrefix);
        String legacyPrefix = LegacyComponentSerializer.legacySection().serialize(prefixComponent);

        // 2. Letzte Farbe für den Namen ermitteln
        ChatColor colorToSet = ChatColor.WHITE;
        String lastColors = ChatColor.getLastColors(legacyPrefix);
        if (!lastColors.isEmpty()) {
            for (int i = lastColors.length() - 2; i >= 0; i -= 2) {
                ChatColor found = ChatColor.getByChar(lastColors.charAt(i + 1));
                if (found != null && found.isColor()) {
                    colorToSet = found;
                    break;
                }
            }
        }

        // 3. Team-Attribute setzen
        // Der Name wird gefärbt, indem wir die Farbe ans Präfix hängen UND setColor nutzen
        team.setPrefix(legacyPrefix + colorToSet);
        team.setColor(colorToSet);

        // Suffix (Vanish)
        if (plugin.getVanishManager().isVanished(player.getUniqueId())) {
            team.setSuffix(ChatColor.GREEN + " [V]");
        } else {
            team.setSuffix("");
        }

        // 4. Spieler-Zuweisung mit Refresh-Logik
        String entry = player.getName();

        // Aus allen Teams entfernen, um Update-Pakete zu erzwingen
        board.getTeams().forEach(t -> {
            if (t.hasEntry(entry)) {
                t.removeEntry(entry);
            }
        });

        team.addEntry(entry);
    }

    public void clearTeams() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        board.getTeams().forEach(team -> {
            if (team.getName().matches("^\\d{3}_.*")) {
                team.unregister();
            }
        });
    }
}