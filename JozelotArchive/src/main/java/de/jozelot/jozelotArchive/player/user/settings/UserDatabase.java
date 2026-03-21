package de.jozelot.jozelotArchive.player.user.settings;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.player.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
}
