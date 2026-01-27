package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class WorldSettings implements Listener {

    private final ConfigManager config;

    public WorldSettings(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) {
        if (!config.isLeafDecay()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (config.isKeepInventory()) {
            event.setKeepInventory(true);
            event.getDrops().clear();
            event.setDroppedExp(0);
        }

        if (!config.isDropItemsOnDeath()) {
            event.getDrops().clear();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!config.isAdvancementsEnabled()) {
            event.getPlayer().setStatistic(org.bukkit.Statistic.ANIMALS_BRED, 0);
        }
    }
}