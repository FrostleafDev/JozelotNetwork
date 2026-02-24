package de.jozelot.jozelotLobby.player;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.items.HiderState;
import org.bukkit.entity.Player;

import javax.sql.rowset.JoinRowSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LobbyPlayerDatabase {

    private final JozelotLobby plugin;

    public LobbyPlayerDatabase(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    public HiderState getHiderState(LobbyPlayer player) {
        UUID uuid = player.getUuid();
        return executeGetHiderState(uuid);
    }

    public HiderState getHiderState(Player player) {
        UUID uuid = player.getUniqueId();
        return executeGetHiderState(uuid);
    }

    /**
     * gettes the current State of the player for the player hider
     * @param uuid
     * @return
     */
    private HiderState executeGetHiderState(UUID uuid) {
        String sql = "SELECT player_hider FROM player_state WHERE uuid = ? LIMIT 1;";

        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return HiderState.valueOf(rs.getString("player_hider").toUpperCase());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return HiderState.VISIBLE;
    }

    /**
     * saves the current State of the player for the player hider
     * @param player
     * @param state
     */
    public void setHiderState(LobbyPlayer player, HiderState state) {
        UUID uuid = player.getUuid();

        String sql = "INSERT INTO player_state (uuid, player_hider) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE player_hider = VALUES(player_hider);";

        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(sql)) {

            insertStmt.setString(1, uuid.toString());
            insertStmt.setString(2, state.name());

            insertStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, HiderState> loadMultipleHiderStates(Collection<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) return Collections.emptyMap();

        Map<UUID, HiderState> results = new HashMap<>();

        String placeholders = String.join(",", Collections.nCopies(uuids.size(), "?"));
        String sql = "SELECT uuid, player_hider FROM player_state WHERE uuid IN (" + placeholders + ");";

        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int i = 1;
            for (UUID uuid : uuids) {
                pstmt.setString(i++, uuid.toString());
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    HiderState state = HiderState.valueOf(rs.getString("player_hider").toUpperCase());
                    results.put(uuid, state);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}
