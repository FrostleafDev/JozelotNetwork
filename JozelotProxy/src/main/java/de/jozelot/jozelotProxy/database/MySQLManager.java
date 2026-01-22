package de.jozelot.jozelotProxy.database;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;
import de.jozelot.jozelotProxy.utils.ConsoleLogger;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MySQLManager {

    private final ConfigManager config;
    private final MySQLSetup mySQLSetup;
    private final ConsoleLogger consoleLogger;

    private final Set<String> registeredServerCache = new HashSet<>();
    private final Map<String, Boolean> maintenanceCache = new HashMap<>();

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
                        "ptero_identifier VARCHAR(64)," +
                        "display_name VARCHAR(32)," +
                        "motd VARCHAR(255) DEFAULT 'Backend not setup'," +
                        "max_players INT DEFAULT 20," +
                        "maintenance BOOLEAN DEFAULT FALSE," +
                        "favicon VARCHAR(64) DEFAULT 'favicon.png'" +
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
                        "identifier VARCHAR(32) UNIQUE," +
                        "display_name VARCHAR(32)," +
                        "scoreboard_enabled BOOLEAN DEFAULT TRUE," +
                        "custom_tab_enabled BOOLEAN DEFAULT TRUE," +
                        "whitelist_active BOOLEAN DEFAULT FALSE" +
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
                        ");",

                "CREATE TABLE IF NOT EXISTS action_logs (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "operator_uuid CHAR(36)," +
                        "action_type VARCHAR(32)," +
                        "target_info VARCHAR(255)," +
                        "details VARCHAR(255)," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "INDEX idx_operator (operator_uuid)," +
                        "INDEX idx_target (target_info)," +
                        "INDEX idx_date (created_at)" +
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

            String identifierProxy = "proxy";

            pstmt.setString(1, identifierProxy);
            pstmt.setString(2, identifierProxy);

            pstmt.addBatch();

            for (RegisteredServer server : servers) {
                String identifier = server.getServerInfo().getName();

                pstmt.setString(1, identifier);
                pstmt.setString(2, identifier);

                pstmt.addBatch();
            }

            pstmt.executeBatch();
            consoleLogger.broadCastToConsole("MariaDB: Server-Synchronisation abgeschlossen (Neue Server wurden hinzugefügt).");
            updateServerCache();

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

    public void updateServerCache() {
        registeredServerCache.clear();
        maintenanceCache.clear();

        String sql = "SELECT identifier, maintenance FROM server;";

        try (Connection conn = mySQLSetup.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String identifier = rs.getString("identifier").toLowerCase();
                boolean isMaintenance = rs.getBoolean("maintenance");

                registeredServerCache.add(identifier);
                maintenanceCache.put(identifier, isMaintenance);
            }

            consoleLogger.broadCastToConsole("MariaDB: Cache wurde aktualisiert ("
                    + registeredServerCache.size() + " Server geladen).");

        } catch (SQLException e) {
            e.printStackTrace();
            consoleLogger.broadCastToConsole("MariaDB: Fehler beim Aktualisieren des Caches!");
        }
    }

    public String getServerDisplayName(String identifier) {
        String sql = "SELECT display_name FROM server WHERE identifier = ? LIMIT 1;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, identifier);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("display_name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return identifier;
    }

    public void setServerDisplayName(String identifier, String display_name) {
        String sql = "UPDATE server SET display_name = ? WHERE identifier = ? LIMIT 1;";

        try (Connection conn = mySQLSetup.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);){

            pstmt.setString(1, display_name);
            pstmt.setString(2, identifier);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getServerMaxPlayers(String identifier) {
        String sql = "SELECT max_players FROM server WHERE identifier = ? LIMIT 1;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, identifier);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("max_players");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setServerMaxPlayers(String identifier, int max_players) {
        String sql = "UPDATE server SET max_players = ? WHERE identifier = ? LIMIT 1;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);){

            pstmt.setInt(1, max_players);
            pstmt.setString(2, identifier);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getServerMOTD(String identifier) {
        String sql = "SELECT motd FROM server WHERE identifier = ? LIMIT 1;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, identifier);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("motd");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setServerMOTD(String identifier, String motd) {
        String sql = "UPDATE server SET motd = ? WHERE identifier = ? LIMIT 1;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);){

            pstmt.setString(1, motd);
            pstmt.setString(2, identifier);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getServerMaintenance(String identifier) {
        String sql = "SELECT maintenance FROM server WHERE identifier = ? LIMIT 1;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, identifier);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("maintenance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setServerMaintenance(String identifier, boolean maintenance) {
        maintenanceCache.put(identifier, maintenance);
        String sql = "UPDATE server SET maintenance = ? WHERE identifier = ? LIMIT 1;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);){

            pstmt.setBoolean(1, maintenance);
            pstmt.setString(2, identifier);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getServerFaviconName(String identifier) {
        String sql = "SELECT favicon FROM server WHERE identifier = ? LIMIT 1;";
        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identifier);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getString("favicon");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public void setServerFaviconName(String identifier, String faviconName) {
        String sql = "UPDATE server SET favicon = ? WHERE identifier = ?;";
        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, faviconName);
            pstmt.setString(2, identifier);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean addPunishment(UUID playerUuid, UUID operatorUuid, String type, String timeStr, String reason) {
        boolean isKick = type.equalsIgnoreCase("KICK");

        if (!isKick) {
            String checkSql = "SELECT id FROM punishment WHERE player_uuid = ? AND type = ? AND is_active = TRUE AND (end_at > CURRENT_TIMESTAMP OR end_at IS NULL);";
            try (Connection conn = mySQLSetup.getConnection();
                 PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, playerUuid.toString());
                checkStmt.setString(2, type);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        Timestamp endAt = null;
        if (!isKick && !timeStr.equalsIgnoreCase("permanent") && !timeStr.equalsIgnoreCase("perma") && !timeStr.equalsIgnoreCase("p")) {
            long durationMillis = parseDuration(timeStr);
            if (durationMillis > 0) {
                endAt = new Timestamp(System.currentTimeMillis() + durationMillis);
            }
        }

        String insertSql = "INSERT INTO punishment (player_uuid, operator_uuid, type, reason, end_at, is_active) VALUES (?, ?, ?, ?, ?, ?);";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            insertStmt.setString(1, playerUuid.toString());
            insertStmt.setString(2, operatorUuid.toString());
            insertStmt.setString(3, type.toUpperCase());
            insertStmt.setString(4, reason);
            insertStmt.setTimestamp(5, endAt);

            insertStmt.setBoolean(6, !isKick);

            insertStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private long parseDuration(String input) {
        try {
            // Trenne Zahl und Buchstabe (z.B. "12h" -> "12" und "h")
            String numberPart = input.replaceAll("[^0-9]", "");
            String unitPart = input.replaceAll("[0-9]", "").toLowerCase();

            if (numberPart.isEmpty()) return -1;
            long amount = Long.parseLong(numberPart);

            return switch (unitPart) {
                case "m" -> amount * 60_000L;               // Minute
                case "h" -> amount * 3_600_000L;            // Stunde
                case "d" -> amount * 86_400_000L;           // Tag
                case "w" -> amount * 604_800_000L;          // Woche
                case "mo" -> amount * 2_592_000_000L;       // Monat (30 Tage)
                case "y" -> amount * 31_536_000_000L;       // Jahr
                default -> -1;
            };
        } catch (Exception e) {
            return -1;
        }
    }

    public UUID getUUIDFromName(String username) {
        String sql = "SELECT uuid FROM player WHERE username = ? LIMIT 1;";
        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Spieler war noch nie auf dem Netzwerk
    }

    public Map<String, String> getActiveBan(UUID uuid) {

        String cleanupSql = "UPDATE punishment SET is_active = FALSE WHERE player_uuid = ? AND is_active = TRUE AND end_at <= CURRENT_TIMESTAMP;";

        String selectSql = "SELECT p.reason, p.end_at, p.operator_uuid, pl.username AS operator_name " +
                "FROM punishment p " +
                "LEFT JOIN player pl ON p.operator_uuid = pl.uuid " +
                "WHERE p.player_uuid = ? AND p.type = 'BAN' AND p.is_active = TRUE LIMIT 1;";

        try (Connection conn = mySQLSetup.getConnection()) {
            try (PreparedStatement cleanupStmt = conn.prepareStatement(cleanupSql)) {
                cleanupStmt.setString(1, uuid.toString());
                cleanupStmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
                pstmt.setString(1, uuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Map<String, String> info = new HashMap<>();
                        info.put("reason", rs.getString("reason"));

                        // Zeit-Formatierung
                        Timestamp endAt = rs.getTimestamp("end_at");
                        info.put("duration", (endAt == null) ? "Permanent" : new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(endAt));

                        // Operator-Name Logik
                        String opUuidStr = rs.getString("operator_uuid");
                        String opName = rs.getString("operator_name");

                        if (opUuidStr.equals("00000000-0000-0000-0000-000000000000")) {
                            info.put("operator", "Konsole");
                        } else {
                            info.put("operator", (opName != null) ? opName : "Unbekannt");
                        }

                        return info;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean removePunishment(UUID playerUuid, String type) {
        String checkSql = "SELECT id FROM punishment WHERE player_uuid = ? AND type = ? AND is_active = TRUE;";
        String updateSql = "UPDATE punishment SET is_active = FALSE WHERE player_uuid = ? AND type = ? AND is_active = TRUE;";

        try (Connection conn = mySQLSetup.getConnection()) {
            boolean hasActiveBan = false;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, playerUuid.toString());
                checkStmt.setString(2, type);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) hasActiveBan = true;
                }
            }

            if (!hasActiveBan) return false;

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, playerUuid.toString());
                updateStmt.setString(2, type);
                updateStmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getBannedPlayerNames() {
        List<String> bannedPlayers = new ArrayList<>();
        String sql = "SELECT DISTINCT p.username FROM player p " +
                "JOIN punishment pun ON p.uuid = pun.player_uuid " +
                "WHERE pun.is_active = TRUE AND pun.type = 'BAN' " +
                "AND (pun.end_at > CURRENT_TIMESTAMP OR pun.end_at IS NULL);";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                bannedPlayers.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bannedPlayers;
    }

    public List<Map<String, String>> getAllActiveBans() {
        List<Map<String, String>> bans = new ArrayList<>();
        String sql = "SELECT p.username AS target_name, op.username AS operator_name, pun.reason, pun.end_at, pun.operator_uuid " +
                "FROM punishment pun " +
                "JOIN player p ON pun.player_uuid = p.uuid " +
                "LEFT JOIN player op ON pun.operator_uuid = op.uuid " +
                "WHERE pun.is_active = TRUE AND pun.type = 'BAN' " +
                "AND (pun.end_at > CURRENT_TIMESTAMP OR pun.end_at IS NULL) " +
                "ORDER BY pun.start_at DESC;";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, String> ban = new HashMap<>();
                ban.put("target", rs.getString("target_name"));

                String opUuid = rs.getString("operator_uuid");
                String opName = rs.getString("operator_name");
                ban.put("operator", opUuid.equals("00000000-0000-0000-0000-000000000000") ? "Konsole" : (opName != null ? opName : "Unbekannt"));

                ban.put("reason", rs.getString("reason"));

                Timestamp endAt = rs.getTimestamp("end_at");
                ban.put("duration", endAt == null ? "Permanent" : new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(endAt));

                bans.add(ban);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bans;
    }

    public void logAction(UUID operator, String type, String target, String details) {
        CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO action_logs (operator_uuid, action_type, target_info, details) VALUES (?, ?, ?, ?);";
            try (Connection conn = mySQLSetup.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, operator.toString());
                pstmt.setString(2, type);
                pstmt.setString(3, target);
                pstmt.setString(4, details);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public String getPteroIdentifier(String serverIdentifier) {
        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT ptero_identifier FROM server WHERE identifier = ?")) {

            ps.setString(1, serverIdentifier);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("ptero_identifier");
                    return (id != null && !id.isEmpty()) ? id : null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadGroups(Map<String, Integer> serverToGroup, Map<Integer, String> groupNames) {
        String query = "SELECT s.identifier as s_id, g.id as g_id, g.display_name as g_name " +
                "FROM server_in_group sig " +
                "JOIN server s ON sig.server_id = s.id " +
                "JOIN server_group g ON sig.group_id = g.id";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                serverToGroup.put(rs.getString("s_id"), rs.getInt("g_id"));
                groupNames.put(rs.getInt("g_id"), rs.getString("g_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isGroupTabEnabled(int groupId) {
        if (groupId == -1) return false;

        String query = "SELECT custom_tab_enabled FROM server_group WHERE id = ?";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("custom_tab_enabled");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    // --- Whitelist Management ---

    public void setWhitelistState(String identifier, boolean active) {
        String query = identifier.equals("proxy")
                ? "UPDATE server_group SET whitelist_active = ? WHERE identifier = 'proxy'"
                : "UPDATE server_group SET whitelist_active = ? WHERE identifier = ?";

        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setBoolean(1, active);
            ps.setString(2, identifier);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isWhitelistActive(String identifier) {
        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT whitelist_active FROM server_group WHERE identifier = ?")) {
            ps.setString(1, identifier);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean("whitelist_active");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addToWhitelist(UUID uuid, int groupId, UUID operatorUuid) {
        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO whitelist (player_uuid, group_id, added_by) VALUES (?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE added_at = CURRENT_TIMESTAMP")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, groupId);
            ps.setString(3, operatorUuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeFromWhitelist(UUID uuid, int groupId) {
        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM whitelist WHERE player_uuid = ? AND group_id = ?")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, groupId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isWhitelisted(UUID uuid, int groupId) {
        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM whitelist WHERE player_uuid = ? AND group_id = ?")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, groupId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getWhitelistPlayers(int groupId) {
        List<String> players = new ArrayList<>();
        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT p.username FROM whitelist w JOIN player p ON w.player_uuid = p.uuid WHERE w.group_id = ?")) {
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) players.add(rs.getString("username"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }

    public UUID getUUIDByUsername(String username) {
        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT uuid FROM player WHERE username = ? LIMIT 1")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return UUID.fromString(rs.getString("uuid"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadGroups(Map<String, Integer> serverToGroup, Map<Integer, String> groupNames, Map<Integer, String> groupIdentifiers) {
        try (Connection conn = mySQLSetup.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT id, identifier, display_name FROM server_group")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    groupIdentifiers.put(id, rs.getString("identifier"));
                    groupNames.put(id, rs.getString("display_name"));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT s.identifier, sig.group_id FROM server s JOIN server_in_group sig ON s.id = sig.server_id")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    serverToGroup.put(rs.getString("identifier"), rs.getInt("group_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getGroupIdByIdentifier(String identifier) {
        try (Connection conn = mySQLSetup.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM server_group WHERE identifier = ?")) {
            ps.setString(1, identifier);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void ensureProxyGroupExists() {
        try (Connection conn = mySQLSetup.getConnection();
        PreparedStatement ps = conn.prepareStatement(
                     "INSERT IGNORE INTO server_group (id, identifier, display_name, whitelist_active) VALUES (0, 'proxy', 'Netzwerk', FALSE)")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isServerInMaintenance(String serverName) {
        return maintenanceCache.getOrDefault(serverName, false);
    }

    public boolean existsInDatabase(String identifier) {
        return registeredServerCache.contains(identifier.toLowerCase());
    }

    public Set<String> getRegisteredServerCache() {
        return registeredServerCache;
    }
}
