package de.jozelot.jozelotLobby.utils;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardManager {

    private final JozelotLobby plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ScoreboardManager(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    public void createScoreboard(Player player) {
        Scoreboard score = player.getScoreboard();

        Objective objective = score.getObjective("lobby");
        if (objective != null) objective.unregister();

        objective = score.registerNewObjective("lobby", Criteria.DUMMY,
                mm.deserialize(plugin.getLang().format("lobby-scoreboard-title", null)));

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.numberFormat(NumberFormat.blank());

        for (int i = 0; i < 15; i++) {
            String teamName = "sb_line_" + i;
            Team team = score.getTeam(teamName);
            if (team == null) team = score.registerNewTeam(teamName);

            String entry = ChatColor.values()[i].toString() + ChatColor.RESET;
            if (!team.hasEntry(entry)) team.addEntry(entry);

            objective.getScore(entry).setScore(i);
        }
        update(player);
    }

    public void startScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                update(player);
            }
        }, 20L, 10L);
    }

    private void update(Player player) {
        LobbyPlayer lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);
        if (lobbyPlayer == null) return;

        List<String> rawLines = plugin.getLang().formatList("lobby-scoreboard", null);
        List<String> formattedLines = new ArrayList<>();

        int foundSecrets = lobbyPlayer.getFoundSecretIds().size();
        int maxSecrets = plugin.getSecretMgr().getSecrets().size();

        String secretColor = foundSecrets == maxSecrets ? "<#00FC00>" : "<#f90036>";

        for (String line : rawLines) {
            String processed = line
                    .replace("{player}", player.getName())
                    .replace("{rank}", lobbyPlayer.getRank())
                    .replace("{color}", lobbyPlayer.getColor().toString())
                    .replace("{playtime}", lobbyPlayer.getFormattedPlaytime())
                    .replace("{secrets}", secretColor +  foundSecrets + "<gray>/" + maxSecrets + secretColor)
                    .replace("{players}", String.valueOf(plugin.getNetworkStateManager().getServer("proxy").players()));
            formattedLines.add(processed);
        }

        render(player, formattedLines);
    }

    private void render(Player player, List<String> lines) {
        Scoreboard score = player.getScoreboard();
        Objective objective = score.getObjective("lobby");
        if (objective == null) return;

        int size = Math.min(lines.size(), 15);

        for (int i = 0; i < 15; i++) {
            Team team = score.getTeam("sb_line_" + (14 - i));
            if (team == null) continue;

            String entry = ChatColor.values()[14 - i].toString() + ChatColor.RESET;

            if (i < size) {
                team.prefix(mm.deserialize(lines.get(i)));
                objective.getScore(entry).setScore(size - i);
            } else {
                score.resetScores(entry);
            }
        }
    }
}