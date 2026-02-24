package de.jozelot.jozelotUtils.utils;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VanishManager {

    private final JozelotUtils plugin;
    private final ConfigManager config;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<UUID, Boolean> vanishedPlayers = new HashMap<>();

    public VanishManager(JozelotUtils plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();

        this.vanishedPlayers.putAll(loadAllVanishedPlayers());
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.containsKey(uuid);
    }

    public void setVanished(UUID uuid, boolean state, boolean isTeamVanish) {
        Player player = Bukkit.getPlayer(uuid);
        boolean previousState = vanishedPlayers.containsKey(uuid);

        if (state) {
            vanishedPlayers.put(uuid, isTeamVanish);
        } else {
            vanishedPlayers.remove(uuid);
        }

        if (player != null) {
            updatePlayerVanish(player);

            if (previousState != state) {
                handleFakeMessages(player, state);
            }
        }
    }

    private void handleFakeMessages(Player player, boolean goingIntoVanish) {
        if (goingIntoVanish) {
            String type = config.getLeaveMessageType();
            if (type.equalsIgnoreCase("default")) {
                Bukkit.broadcast(mm.deserialize("<yellow>" + player.getName() + " left the game"));
            } else if (type.equalsIgnoreCase("custom")) {
                String msg = lang.format("leave-message", Map.of("player-name", player.getName()));
                if (msg != null) Bukkit.broadcast(mm.deserialize(msg));
            }
        } else {
            String type = config.getJoinMessageType();
            if (type.equalsIgnoreCase("default")) {
                Bukkit.broadcast(mm.deserialize("<yellow>" + player.getName() + " joined the game"));
            } else if (type.equalsIgnoreCase("custom")) {
                String msg = lang.format("join-message", Map.of("player-name", player.getName()));
                if (msg != null) Bukkit.broadcast(mm.deserialize(msg));
            }
        }
    }

    public void updateAllForPlayer(Player viewer) {
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (isVanished(target.getUniqueId())) {
                if (canSee(viewer, target)) {
                    viewer.showPlayer(plugin, target);
                } else {
                    viewer.hidePlayer(plugin, target);
                }
            }
        }
    }

    public void updatePlayerVanish(Player player) {
        boolean vanished = isVanished(player.getUniqueId());

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (vanished && !canSee(viewer, player)) {
                viewer.hidePlayer(plugin, player);
            } else {
                viewer.showPlayer(plugin, player);
            }
        }

        player.setGlowing(vanished);
        player.setCollidable(!vanished);
        player.setInvulnerable(vanished);

        plugin.getPlayerNameTag().updateNametag(player);
    }

    public boolean canSee(Player viewer, Player target) {
        UUID targetUUID = target.getUniqueId();
        if (viewer.equals(target)) return true;
        if (!isVanished(targetUUID)) return true;
        if (viewer.hasPermission("network.vanish.see-all")) return true;

        boolean isTeamVanish = vanishedPlayers.getOrDefault(targetUUID, false);
        return isTeamVanish && viewer.hasPermission("network.vanish.see-team");
    }

    public Map<UUID, Boolean> loadAllVanishedPlayers() {
        Map<UUID, Boolean> results = new HashMap<>();
        String sql = "SELECT uuid FROM player_state WHERE is_vanish = 1;";

        try (Connection conn = plugin.getMySQLSetup().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                results.put(uuid, false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
}