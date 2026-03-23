package de.jozelot.jozelotArchive.player.user;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.inventory.hotbar.items.HiderState;
import de.jozelot.jozelotArchive.player.user.settings.Setting;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserDatabase {

    private final JozelotArchive plugin;

    public UserDatabase(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public Map<Setting, String> getAllSettings(User user) {
        Map<Setting, String> settingsMap = new HashMap<>();
        String sql = "SELECT setting_key, setting_value FROM player_settings WHERE uuid = ?;";

        try (Connection conn = plugin.getServiceManager().getSQLConnection().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUniqueId().toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("setting_key");
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

    public void setHiderState(User user, HiderState state) {
        UUID uuid = user.getUniqueId();

        String sql = "INSERT INTO player_state (uuid, player_hider) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE player_hider = VALUES(player_hider);";

        try (Connection conn = plugin.getServiceManager().getSQLConnection().getConnection();
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

        try (Connection conn = plugin.getServiceManager().getSQLConnection().getConnection();
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

    public HiderState getHiderState(Player player) {
        UUID uuid = player.getUniqueId();
        return executeGetHiderState(uuid);
    }
    public HiderState getHiderState(UUID uuid) {
        return executeGetHiderState(uuid);
    }

    /**
     * gettes the current State of the player for the player hider
     * @param uuid
     * @return
     */
    private HiderState executeGetHiderState(UUID uuid) {
        String sql = "SELECT player_hider FROM player_state WHERE uuid = ? LIMIT 1;";

        try (Connection conn = plugin.getServiceManager().getSQLConnection().getConnection();
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
}
