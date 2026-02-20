package de.jozelot.jozelotProxy.utils;

import com.velocitypowered.api.proxy.Player;
import de.jozelot.jozelotProxy.JozelotProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final JozelotProxy plugin;
    private final Map<UUID, Boolean> vanishedPlayers = new HashMap<>();

    public VanishManager(JozelotProxy plugin) {
        this.plugin = plugin;
    }

    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.containsKey(uuid);
    }

    public void toggleVanish(Player player, boolean teamFlag) {
        UUID uuid = player.getUniqueId();
        boolean newState = !vanishedPlayers.containsKey(uuid);

        if (newState) {
            vanishedPlayers.put(uuid, teamFlag);
        } else {
            vanishedPlayers.remove(uuid);
        }

        // MySQL & Redis
        plugin.getMySQLManager().updateVanishStatus(uuid, newState);

        plugin.getRedisManager().publish("network:vanish", uuid + ":" + newState + ":" + teamFlag);

        plugin.getServerSwitchListener().updateAllTabs();
    }

    public void setVanished(UUID uuid, boolean teamFlag) {
        vanishedPlayers.put(uuid, teamFlag);
    }

    public boolean canSee(Player viewer, Player target) {
        UUID targetUUID = target.getUniqueId();

        if (!isVanished(targetUUID)) return true;

        if (viewer.hasPermission("network.vanish.see-all")) return true;

        boolean isTeamVanish = vanishedPlayers.getOrDefault(targetUUID, false);
        if (isTeamVanish && viewer.hasPermission("network.vanish.see-team")) return true;

        int viewerWeight = plugin.getLuckpermsUtils().getWeight(viewer);
        int targetWeight = plugin.getLuckpermsUtils().getWeight(target);

        return isVanished(viewer.getUniqueId()) && viewerWeight >= targetWeight;
    }

    public Set<UUID> getVanishedPlayers() {
        return vanishedPlayers.keySet();
    }
}