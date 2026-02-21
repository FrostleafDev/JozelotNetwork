package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class VanishListener implements Listener {

    private final JozelotUtils plugin;

    public VanishListener(JozelotUtils plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.getVanishManager().isVanished(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMobTarget(EntityTargetEvent event) {
        // Das Target ist das Ziel des Mobs
        if (event.getTarget() instanceof Player player) {
            if (plugin.getVanishManager().isVanished(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.getVanishManager().isVanished(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        if (plugin.getVanishManager().isVanished(event.getPlayer().getUniqueId())) {
            event.message(null);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.getVanishManager().isVanished(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    /*@EventHandler
    public void onChestOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getVanishManager().isVanished(player.getUniqueId())) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getState() instanceof Container
                || block.getType() == Material.ENDER_CHEST) {

            event.setCancelled(true);

            if (block.getType() == Material.ENDER_CHEST) {
                player.openInventory(player.getEnderChest());
            } else {
                Container container = (Container) block.getState();
                player.openInventory(container.getInventory());
            }
        }
    }*/
}