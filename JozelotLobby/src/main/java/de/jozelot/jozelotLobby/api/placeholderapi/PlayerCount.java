package de.jozelot.jozelotLobby.api.placeholderapi;

import de.jozelot.jozelotLobby.JozelotLobby;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerCount extends PlaceholderExpansion {

    private final JozelotLobby plugin;

    public PlayerCount(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Jozelot";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "network";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("players_challenge_all")) {
            String[] challengeServers = {"challenge-1", "challenge-2", "challenge-3"};
            int totalPlayers = 0;
            boolean anyOnline = false;

            for (String id : challengeServers) {
                var state = plugin.getNetworkStateManager().getServer(id);
                if (state != null && state.online()) {
                    totalPlayers += state.players();
                    anyOnline = true;
                }
            }

            if (!anyOnline) {
                return "<#f90036>0";
            }

            String color = (totalPlayers > 0) ? "<#00FC00>" : "<#f90036>";
            return color + totalPlayers;
        }
        // Syntax: %network_players_<serverid>%
        if (params.startsWith("players_")) {
            String serverId = params.replace("players_", "");
            var state = plugin.getNetworkStateManager().getServer(serverId);

            if (state == null || !state.online()) {
                return "<#f90036>0";
            }

            String color = (state.players() > 0) ? "<#00FC00>" : "<#f90036>";
            return color + state.players();
        }

        // Syntax: %network_status_<serverid>%
        if (params.startsWith("status_")) {
            String serverId = params.replace("status_", "");
            var state = plugin.getNetworkStateManager().getServer(serverId);
            return (state != null && state.online()) ? "Online" : "Offline";
        }

        return null;
    }
}