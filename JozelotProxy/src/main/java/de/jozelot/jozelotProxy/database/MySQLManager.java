package de.jozelot.jozelotProxy.database;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;

import java.sql.*;
import java.util.*;

public class MySQLManager {

    private final ConfigManager config;
    private final MySQLSetup mySQLSetup;
    private final ConsoleLogger consoleLogger;

    public MySQLManager(JozelotProxy plugin) {
        this.config = plugin.getConfig();
        this.mySQLSetup = plugin.getMySQLSetup();
        this.consoleLogger = plugin.getConsoleLogger();
    }

    public void createTables() {
        String[] queries = {
                // 1. Server Tabelle
                "CREATE TABLE IF NOT EXISTS server (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "identifier VARCHAR(32) UNIQUE," +
                        "display_name VARCHAR(32)" +
                        ");",

                // 2. Player Tabelle
                "CREATE TABLE IF NOT EXISTS player (" +
                        "uuid CHAR(36) PRIMARY KEY," +
                        "username VARCHAR(16)," +
                        "first_join TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "last_join TIMESTAMP NULL," +
                        "server_id INT," +
                        "INDEX (username)," +
                        "FOREIGN KEY (server_id) REFERENCES server(id) ON DELETE SET NULL" +
                        ");",

                // 3. Player State
                "CREATE TABLE IF NOT EXISTS player_state (" +
                        "uuid CHAR(36) PRIMARY KEY," +
                        "is_vanish BOOLEAN DEFAULT FALSE," +
                        "is_spy BOOLEAN DEFAULT FALSE," +
                        "FOREIGN KEY (uuid) REFERENCES player(uuid) ON DELETE CASCADE" +
                        ");",

                // 4. Punishment
                "CREATE TABLE IF NOT EXISTS punishment (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "player_uuid CHAR(36)," +
                        "operator_uuid CHAR(36)," +
                        "type ENUM('BAN', 'KICK')," +
                        "reason VARCHAR(400)," +
                        "start_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "end_at TIMESTAMP NULL," +
                        "is_active BOOLEAN DEFAULT TRUE," +
                        "INDEX (player_uuid)," +
                        "FOREIGN KEY (player_uuid) REFERENCES player(uuid) ON DELETE CASCADE" +
                        ");",

                // 5. Server Groups
                "CREATE TABLE IF NOT EXISTS server_group (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "identifier VARCHAR(32) UNIQUE" +
                        ");",

                // 6. Server in Group (Mapping Tabelle)
                "CREATE TABLE IF NOT EXISTS server_in_group (" +
                        "server_id INT," +
                        "group_id INT," +
                        "PRIMARY KEY (server_id, group_id)," +
                        "FOREIGN KEY (server_id) REFERENCES server(id) ON DELETE CASCADE," +
                        "FOREIGN KEY (group_id) REFERENCES server_group(id) ON DELETE CASCADE" +
                        ");",

                // 7. Secrets
                "CREATE TABLE IF NOT EXISTS secret (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "name VARCHAR(32)," +
                        "description VARCHAR(64)" +
                        ");",

                // 8. Secrets Found
                "CREATE TABLE IF NOT EXISTS secret_found (" +
                        "player_uuid CHAR(36)," +
                        "secret_id INT," +
                        "found_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "PRIMARY KEY (player_uuid, secret_id)," +
                        "FOREIGN KEY (player_uuid) REFERENCES player(uuid) ON DELETE CASCADE," +
                        "FOREIGN KEY (secret_id) REFERENCES secret(id) ON DELETE CASCADE" +
                        ");",

                "CREATE TABLE IF NOT EXISTS whitelist (" +
                        "player_uuid CHAR(36)," +
                        "group_id INT," +
                        "added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "added_by CHAR(36)," +
                        "PRIMARY KEY (player_uuid, group_id)," +
                        "FOREIGN KEY (group_id) REFERENCES server_group(id) ON DELETE CASCADE" +
                        ");"
        };

        try (Connection conn = mySQLSetup.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : queries) {
                stmt.execute(sql);
            }

            consoleLogger.broadCastToConsole("Mariadb: Tabellen wurden erstellt");
        } catch (SQLException e) {
            e.printStackTrace();

            consoleLogger.broadCastToConsole("Mariadb: Tabellen erstellen gescheitert");
        }
    }

    public boolean addToPlayerList(UUID uuid, String username) {
        String sqlPlayer = "INSERT INTO player (uuid, username, last_join) VALUES (?, ?, CURRENT_TIMESTAMP) " +
                "ON DUPLICATE KEY UPDATE username = ?, last_join = CURRENT_TIMESTAMP;";

        String sqlState = "INSERT IGNORE INTO player_state (uuid) VALUES (?);";

        try (Connection conn = mySQLSetup.getConnection()) {
            int affectedRows;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPlayer)) {
                pstmt.setString(1, uuid.toString());
                pstmt.setString(2, username);
                pstmt.setString(3, username);
                affectedRows = pstmt.executeUpdate();
            }

            try (PreparedStatement pstmtState = conn.prepareStatement(sqlState)) {
                pstmtState.setString(1, uuid.toString());
                pstmtState.executeUpdate();
            }

            return (affectedRows == 1);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public void registerAllServers(Collection<RegisteredServer> servers) {
        String sql = "INSERT IGNORE INTO server (identifier, display_name) VALUES (?, ?);";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (RegisteredServer server : servers) {
                String identifier = server.getServerInfo().getName();

                pstmt.setString(1, identifier);
                pstmt.setString(2, identifier);

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            consoleLogger.broadCastToConsole("MariaDB: Server-Synchronisation abgeschlossen (Neue Server wurden hinzugefügt).");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePlayerServer(UUID uuid, String serverIdentifier) {
        String sql = "UPDATE player SET server_id = (SELECT id FROM server WHERE identifier = ?) WHERE uuid = ?;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, serverIdentifier);
            pstmt.setString(2, uuid.toString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getOfflinePlayerInfo(String username) {
        String sql = "SELECT p.username, s.display_name " +
                "FROM player p " +
                "LEFT JOIN server s ON p.server_id = s.id " +
                "WHERE p.username = ? LIMIT 1;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String foundName = rs.getString("username");
                    String lastServer = rs.getString("display_name");

                    if (lastServer == null) lastServer = "Unbekannt";

                    return Map.of("name", foundName, "server", lastServer);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Spieler war noch nie da
    }
    public List<String> getSimilarOfflinePlayers(String search) {
        List<String> names = new ArrayList<>();
        String sql = "SELECT username FROM player WHERE username LIKE ? LIMIT 10;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, search + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    names.add(rs.getString("username"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }
}
