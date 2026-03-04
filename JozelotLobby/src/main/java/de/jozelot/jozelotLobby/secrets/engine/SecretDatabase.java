package de.jozelot.jozelotLobby.secrets.engine;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.secrets.objects.Secret;
import de.jozelot.jozelotLobby.secrets.objects.SecretRegion;
import org.bukkit.util.Vector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SecretDatabase {

    private final JozelotLobby plugin;

    public SecretDatabase(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    public List<Secret> loadAllSecrets() {
        List<Secret> secrets = new ArrayList<>();
        String query = "SELECT * FROM secret";

        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Secret secret = new Secret(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("block")
                );
                loadRegionsForSecret(secret);
                secrets.add(secret);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return secrets;
    }

    // In SecretDatabase.java
    private void loadRegionsForSecret(Secret secret) {
        String query = "SELECT * FROM secret_region WHERE secret_id = ?";
        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, secret.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vector v1 = new Vector(rs.getInt("min_x"), rs.getInt("min_y"), rs.getInt("min_z"));
                Vector v2 = new Vector(rs.getInt("max_x"), rs.getInt("max_y"), rs.getInt("max_z"));
                String worldName = rs.getString("world"); // Hier den echten Namen laden!

                secret.getRegions().add(new SecretRegion(v1, v2, worldName));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<Integer> getFoundSecretIds(UUID uuid) {
        Set<Integer> ids = new HashSet<>();
        String query = "SELECT secret_id FROM secret_found WHERE player_uuid = ?;";

        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("secret_id"));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Laden der gefundenen Secrets: " + e.getMessage());
        }
        return ids;
    }

    public void saveAllFoundSecrets(UUID uuid, Set<Integer> secretIds) {
        String deleteQuery = "DELETE FROM secret_found WHERE player_uuid = ?;";
        String insertQuery = "INSERT INTO secret_found (player_uuid, secret_id) VALUES (?, ?);";

        try (Connection conn = plugin.getMySQLSetup().getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deletePs = conn.prepareStatement(deleteQuery)) {
                deletePs.setString(1, uuid.toString());
                deletePs.executeUpdate();
            }

            try (PreparedStatement insertPs = conn.prepareStatement(insertQuery)) {
                for (int id : secretIds) {
                    insertPs.setString(1, uuid.toString());
                    insertPs.setInt(2, id);
                    insertPs.addBatch();
                }
                insertPs.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Full-Sync der Secrets für " + uuid + ": " + e.getMessage());
        }
    }

    public void saveFoundSecret(UUID uuid, int secretId) {
        String query = "INSERT IGNORE INTO secret_found (player_uuid, secret_id) VALUES (?, ?);";

        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, uuid.toString());
            ps.setInt(2, secretId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim sofortigen Speichern des Secrets: " + e.getMessage());
        }
    }

    public int createSecret(String name, String description, String block, SecretRegion region) {
        String insertSecret = "INSERT INTO secret (name, description, block) VALUES (?, ?, ?);";
        String insertRegion = "INSERT INTO secret_region (secret_id, min_x, min_y, min_z, max_x, max_y, max_z, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = plugin.getMySQLSetup().getConnection()) {
            conn.setAutoCommit(false); // Transaktion starten

            int secretId;
            try (PreparedStatement ps = conn.prepareStatement(insertSecret, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, description);
                ps.setString(3, block);
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (!rs.next()) {
                    conn.rollback();
                    return -1;
                }
                secretId = rs.getInt(1);
            }

            try (PreparedStatement ps = conn.prepareStatement(insertRegion)) {
                ps.setInt(1, secretId);
                ps.setInt(2, region.getMin().getBlockX());
                ps.setInt(3, region.getMin().getBlockY());
                ps.setInt(4, region.getMin().getBlockZ());
                ps.setInt(5, region.getMax().getBlockX());
                ps.setInt(6, region.getMax().getBlockY());
                ps.setInt(7, region.getMax().getBlockZ());
                ps.setString(8, region.getWorldName());
                ps.executeUpdate();
            }

            conn.commit();
            return secretId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void deleteSecret(int id) {
        // In dieser Reihenfolge löschen, um SQL-Constraints einzuhalten
        String[] queries = {
                "DELETE FROM secret_found WHERE secret_id = ?;",
                "DELETE FROM secret_region WHERE secret_id = ?;",
                "DELETE FROM secret WHERE id = ?;"
        };

        try (Connection conn = plugin.getMySQLSetup().getConnection()) {
            conn.setAutoCommit(false);

            for (String query : queries) {
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addRegionToSecret(int secretId, SecretRegion region) {
        String query = "INSERT INTO secret_region (secret_id, min_x, min_y, min_z, max_x, max_y, max_z, world) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, secretId);
            ps.setInt(2, region.getMin().getBlockX());
            ps.setInt(3, region.getMin().getBlockY());
            ps.setInt(4, region.getMin().getBlockZ());
            ps.setInt(5, region.getMax().getBlockX());
            ps.setInt(6, region.getMax().getBlockY());
            ps.setInt(7, region.getMax().getBlockZ());
            ps.setString(8, region.getWorldName());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Hinzufügen einer Region: " + e.getMessage());
        }
    }

    public void removeRegions(int secretId) {
        String query = "DELETE FROM secret_region WHERE secret_id = ?;";
        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, secretId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSecretValue(int id, String column, String value) {
        String query = "UPDATE secret SET " + column + " = ? WHERE id = ?;";
        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, value);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Fehler beim Update von " + column + ": " + e.getMessage());
        }
    }
}
