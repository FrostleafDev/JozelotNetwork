package de.jozelot.jozelotLobby.database;

import de.jozelot.jozelotLobby.JozelotLobby;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MySQLManager {

    private final JozelotLobby plugin;
    private final MySQLSetup mySQLSetup;

    public MySQLManager(JozelotLobby plugin) {
        this.plugin = plugin;
        this.mySQLSetup = plugin.getMySQLSetup();
    }

    /**
     * Holt die Spielzeiten aller Server für einen Spieler.
     * Nutzt einen JOIN, um direkt den display_name aus der server-Tabelle zu erhalten.
     */
    public Map<String, Long> getAllServerPlaytimes(UUID uuid) {
        Map<String, Long> times = new HashMap<>();
        // COALESCE nutzt den identifier, falls display_name NULL ist
        String query = "SELECT COALESCE(s.display_name, s.identifier) AS name, ps.total_playtime " +
                "FROM playtime_server ps " +
                "JOIN server s ON ps.server_id = s.id " +
                "WHERE ps.player_uuid = ? " +
                "ORDER BY ps.total_playtime DESC;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    times.put(rs.getString("name"), rs.getLong("total_playtime"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Laden der Server-Spielzeiten: " + e.getMessage());
        }
        return times;
    }

    public long getTotalNetworkPlaytime(UUID uuid) {
        String query = "SELECT SUM(total_playtime) FROM playtime_server WHERE player_uuid = ?;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Berechnen der Gesamtspielzeit: " + e.getMessage());
        }
        return 0;
    }
}