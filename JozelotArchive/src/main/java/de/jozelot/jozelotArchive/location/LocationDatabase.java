package de.jozelot.jozelotArchive.location;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.player.archivedPlayer.ArchivedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.sql.*;
import java.util.*;

public class LocationDatabase {

    private final JozelotArchive plugin;

    public LocationDatabase(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    /**
     * Speichert eine Location in der Datenbank.
     * Nutzt ON DUPLICATE KEY UPDATE für die ID.
     */
    public int saveLocation(Location location) {
        String sql = "INSERT INTO archive_locations (id, server_id, type, name, description, owner_uuid, world_name, " +
                "min_x, min_y, min_z, max_x, max_y, max_z) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE type = VALUES(type), name = VALUES(name), description = VALUES(description), " +
                "owner_uuid = VALUES(owner_uuid), world_name = VALUES(world_name), " +
                "min_x = VALUES(min_x), min_y = VALUES(min_y), min_z = VALUES(min_z), " +
                "max_x = VALUES(max_x), max_y = VALUES(max_y), max_z = VALUES(max_z);";

        try (Connection conn = plugin.getServiceManager().getSQLConnection().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (location.getId() <= 0) {
                pstmt.setNull(1, Types.INTEGER);
            } else {
                pstmt.setInt(1, location.getId());
            }

            int serverId = plugin.getServiceManager().getConfigManager().getInt("server-id");
            pstmt.setInt(2, serverId);
            pstmt.setString(3, location.getType().name());
            pstmt.setString(4, location.getName());
            pstmt.setString(5, location.getDescription());
            pstmt.setString(6, location.getOwner() != null ? location.getOwner().getUniqueId().toString() : null);

            LocationArea area = location.getArea();
            pstmt.setString(7, area.getWorld().getName());
            pstmt.setDouble(8, area.getMinX());
            pstmt.setDouble(9, area.getMinY());
            pstmt.setDouble(10, area.getMinZ());
            pstmt.setDouble(11, area.getMaxX());
            pstmt.setDouble(12, area.getMaxY());
            pstmt.setDouble(13, area.getMaxZ());

            pstmt.executeUpdate();

            int id = location.getId();
            if (id <= 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        id = generatedKeys.getInt(1);
                    }
                }
            }

            saveMembers(location);
            return id;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<Location> getAllLocations() {
        List<Location> list = new ArrayList<>();
        int serverId = plugin.getServiceManager().getConfigManager().getInt("server-id");
        String sql = "SELECT * FROM archive_locations WHERE server_id = ?;";

        try (Connection conn = plugin.getServiceManager().getSQLConnection().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, serverId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    LocationType type = LocationType.valueOf(rs.getString("type").toUpperCase());
                    String name = rs.getString("name");
                    String desc = rs.getString("description");

                    String ownerUuidStr = rs.getString("owner_uuid");
                    ArchivedPlayer owner = null;
                    if (ownerUuidStr != null) {
                        // Hier müsstest du eine Methode haben, die einen ArchivedPlayer aus einer UUID macht
                        // Oder du specherst in der Location-Klasse vorerst nur die UUID.
                    }

                    World world = Bukkit.getWorld(rs.getString("world_name"));
                    if (world == null) continue;

                    LocationArea area = new LocationArea(
                            rs.getDouble("min_x"), rs.getDouble("min_y"), rs.getDouble("min_z"),
                            rs.getDouble("max_x"), rs.getDouble("max_y"), rs.getDouble("max_z"),
                            world
                    );

                    Location loc = new Location(id, type, name, desc, owner, new HashSet<>(), area);
                    list.add(loc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteLocation(int id) {
        String sql = "DELETE FROM archive_locations WHERE id = ?;";
        try (Connection conn = plugin.getServiceManager().getSQLConnection().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveMembers(Location location) {
        String deleteSql = "DELETE FROM archive_location_members WHERE location_id = ?;";
        String insertSql = "INSERT INTO archive_location_members (location_id, player_uuid) VALUES (?, ?);";

        try (Connection conn = plugin.getServiceManager().getSQLConnection().getConnection()) {
            try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
                deletePstmt.setInt(1, location.getId());
                deletePstmt.executeUpdate();
            }

            if (!location.getMembers().isEmpty()) {
                try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                    for (var member : location.getMembers()) {
                        insertPstmt.setInt(1, location.getId());
                        insertPstmt.setString(2, member.getUniqueId().toString());
                        insertPstmt.addBatch();
                    }
                    insertPstmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Set<UUID> loadMemberUUIDs(int locationId) {
        Set<UUID> members = new HashSet<>();
        String sql = "SELECT player_uuid FROM archive_location_members WHERE location_id = ?;";

        try (Connection conn = plugin.getServiceManager().getSQLConnection().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, locationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    members.add(UUID.fromString(rs.getString("player_uuid")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }
}