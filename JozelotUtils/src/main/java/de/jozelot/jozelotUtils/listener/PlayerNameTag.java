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

        // LuckPerms Event Listener registrieren
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

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        LuckPerms api = LuckPermsProvider.get();
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return;

        String prefix = user.getCachedData().getMetaData().getPrefix();
        if (prefix == null) prefix = "";

        int weight = 0;
        if (api.getGroupManager().getGroup(user.getPrimaryGroup()) != null) {
            weight = api.getGroupManager().getGroup(user.getPrimaryGroup()).getWeight().orElse(0);
        }

        String teamName = String.format("%03d_%s", (999 - weight), user.getPrimaryGroup());
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);

        Team team = board.getTeam(teamName);
        if (team == null) {
            team = board.registerNewTeam(teamName);
        }

        Component prefixComponent = mm.deserialize(prefix);
        team.prefix(prefixComponent);
        TextColor lastColor = findLastColor(prefixComponent);

        if (lastColor != null) {
            team.color(NamedTextColor.nearestTo(lastColor));
        } else {
            team.color(NamedTextColor.WHITE);
        }

        if (!team.hasEntry(player.getName())) {
            board.getTeams().forEach(t -> {
                if (t.hasEntry(player.getName())) t.removeEntry(player.getName());
            });
            team.addEntry(player.getName());
        }
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
}