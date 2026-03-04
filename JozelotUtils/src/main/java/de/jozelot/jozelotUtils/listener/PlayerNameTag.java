package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
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

    public void clearTeams() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        board.getTeams().forEach(team -> {
            if (team.getName().matches("^\\d{3}_.*")) {
                team.unregister();
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

        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        // 1. Daten vorbereiten
        String prefix = user.getCachedData().getMetaData().getPrefix() != null ? user.getCachedData().getMetaData().getPrefix() : "";
        int weight = api.getGroupManager().getGroup(user.getPrimaryGroup()) != null ? api.getGroupManager().getGroup(user.getPrimaryGroup()).getWeight().orElse(0) : 0;

        String teamName = String.format("%03d_%s", (999 - weight), user.getPrimaryGroup());
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);

        Component prefixComponent = mm.deserialize(prefix);
        TextColor lastColor = findLastColor(prefixComponent);
        NamedTextColor teamColor = (lastColor != null) ? NamedTextColor.nearestTo(lastColor) : NamedTextColor.WHITE;

        // 2. Dieses Team in JEDES Scoreboard auf dem Server schreiben
        for (Player online : Bukkit.getOnlinePlayers()) {
            Scoreboard board = online.getScoreboard();
            updateTeamInBoard(board, player, teamName, prefixComponent, teamColor);
        }

        // Auch im MainScoreboard aktuell halten (für neue Spieler)
        updateTeamInBoard(Bukkit.getScoreboardManager().getMainScoreboard(), player, teamName, prefixComponent, teamColor);
    }

    private TextColor findLastColor(Component component) {
        TextColor lastColor = component.color();
        for (Component child : component.children()) {
            TextColor childColor = findLastColor(child);
            if (childColor != null) {
                lastColor = childColor;
            }
        }
        return lastColor;
    }

    private void updateTeamInBoard(Scoreboard board, Player target, String teamName, Component prefix, NamedTextColor color) {
        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);

        team.prefix(prefix);
        team.color(color);

        if (plugin.getVanishManager().isVanished(target.getUniqueId())) {
            team.suffix(mm.deserialize(" <#00FC00>[V]"));
        } else {
            team.suffix(Component.empty());
        }

        if (!team.hasEntry(target.getName())) {
            board.getTeams().forEach(t -> {
                if (t.getName().matches("^\\d{3}_.*") && t.hasEntry(target.getName())) {
                    t.removeEntry(target.getName());
                }
            });
            team.addEntry(target.getName());
        }
    }
}