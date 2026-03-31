package de.jozelot.jozelotLobby.player;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.settings.Setting;
import de.jozelot.jozelotLobby.ui.items.HiderState;
import org.bukkit.entity.Player;

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

    public void setSetting(LobbyPlayer player, Setting setting, String value) {
        UUID uuid = player.getUuid();

        // Uppercase Key
        String key = setting.getKey().toUpperCase();

        if (setting.getDefaultValue().equalsIgnoreCase(value)) {
            String sql = "DELETE FROM player_settings WHERE uuid = ? AND setting_key = ?;";

            try (Connection conn = plugin.getMySQLSetup().getConnection();
                 PreparedStatement deleteStmt = conn.prepareStatement(sql)) {
                deleteStmt.setString(1, uuid.toString());
                deleteStmt.setString(2, key); // Nutzen des Uppercase Keys
                deleteStmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        String sql = "INSERT INTO player_settings (uuid, setting_key, setting_value) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);";

        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(sql)) {

            insertStmt.setString(1, uuid.toString());
            insertStmt.setString(2, key); // Uppercase
            insertStmt.setString(3, value);

            insertStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setSettings(LobbyPlayer player, Map<Setting, String> settingStringMap) {
        if (settingStringMap == null || settingStringMap.isEmpty()) return;

        String uuidStr = player.getUuid().toString();

        String sqlInsert = "INSERT INTO player_settings (uuid, setting_key, setting_value) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);";
        String sqlDelete = "DELETE FROM player_settings WHERE uuid = ? AND setting_key = ?;";

        try (Connection conn = plugin.getMySQLSetup().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsert);
                 PreparedStatement deleteStmt = conn.prepareStatement(sqlDelete)) {

                for (Map.Entry<Setting, String> entry : settingStringMap.entrySet()) {
                    Setting setting = entry.getKey();
                    String value = entry.getValue();
                    String key = setting.getKey().toUpperCase(); // Uppercase

                    if (setting.getDefaultValue().equalsIgnoreCase(value)) {
                        deleteStmt.setString(1, uuidStr);
                        deleteStmt.setString(2, key);
                        deleteStmt.addBatch();
                    } else {
                        insertStmt.setString(1, uuidStr);
                        insertStmt.setString(2, key);
                        insertStmt.setString(3, value);
                        insertStmt.addBatch();
                    }
                }
                insertStmt.executeBatch();
                deleteStmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getSetting(LobbyPlayer player, Setting setting) {
        /* if (ergebnis.equals(setting.getDefaultValue()) {
            return setting.getDefaultValue();
        }
        */
        return setting.getDefaultValue();
    }

    public Map<Setting, String> getAllSettings(LobbyPlayer player) {
        Map<Setting, String> settingsMap = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM player_settings WHERE uuid = ?;";

        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, player.getUuid().toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("setting_key").toUpperCase();
                    String value = rs.getString("setting_value");

                    Setting setting = Setting.fromKey(key);
                    if (setting != null) {
                        settingsMap.put(setting, value);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return settingsMap;
    }

    public void saveAllPlayerSettings(Collection<LobbyPlayer> players) {
        if (players == null || players.isEmpty()) return;

        String sqlHider = "INSERT INTO player_state (uuid, player_hider) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE player_hider = VALUES(player_hider);";

        String sqlInsertSetting = "INSERT INTO player_settings (uuid, setting_key, setting_value) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value);";

        String sqlDeleteSetting = "DELETE FROM player_settings WHERE uuid = ? AND setting_key = ?;";

        try (Connection conn = plugin.getMySQLSetup().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement hiderStmt = conn.prepareStatement(sqlHider);
                 PreparedStatement insertSetStmt = conn.prepareStatement(sqlInsertSetting);
                 PreparedStatement deleteSetStmt = conn.prepareStatement(sqlDeleteSetting)) {

                for (LobbyPlayer lp : players) {
                    String uuidStr = lp.getUuid().toString();
                    hiderStmt.setString(1, uuidStr);
                    hiderStmt.setString(2, lp.getHiderState().name());
                    hiderStmt.addBatch();

                    for (Map.Entry<Setting, String> entry : lp.getSettings().entrySet()) {
                        Setting setting = entry.getKey();
                        String value = entry.getValue();
                        String key = setting.getKey().toUpperCase(); // Uppercase

                        if (setting.getDefaultValue().equalsIgnoreCase(value)) {
                            deleteSetStmt.setString(1, uuidStr);
                            deleteSetStmt.setString(2, key);
                            deleteSetStmt.addBatch();
                        } else {
                            insertSetStmt.setString(1, uuidStr);
                            insertSetStmt.setString(2, key);
                            insertSetStmt.setString(3, value);
                            insertSetStmt.addBatch();
                        }
                    }
                }
                hiderStmt.executeBatch();
                insertSetStmt.executeBatch();
                deleteSetStmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Long> getAllServerPlaytimes(UUID uuid) {
        Map<String, Long> times = new HashMap<>();
        String query = "SELECT s.display_name, ps.total_playtime " +
                "FROM playtime_server ps " +
                "JOIN server s ON ps.server_id = s.id " +
                "WHERE ps.player_uuid = ?;";

        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("display_name");
                    long time = rs.getLong("total_playtime");
                    times.put(name, time);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Laden der Server-Spielzeiten: " + e.getMessage());
        }
        return times;
    }
}
