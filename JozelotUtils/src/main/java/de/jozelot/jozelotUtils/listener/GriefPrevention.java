package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class GriefPrevention implements Listener {

    private final ConfigManager config;

    public GriefPrevention(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.canBuild() && !(event.getPlayer().hasPermission("network.utils.admin.build") && event.getPlayer().getGameMode() == GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!config.canBuild() && !(event.getPlayer().hasPermission("network.utils.admin.build") && event.getPlayer().getGameMode() == GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (config.canBuild()) return;
        if (event.getPlayer().hasPermission("network.utils.admin.build") && !event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;

        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!event.getClickedBlock().getType().isInteractable()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (config.canBuild()) return;
        if (event.getEntityType() == EntityType.ITEM_FRAME || event.getEntityType() == EntityType.ARMOR_STAND) {
            if (!event.getDamager().hasPermission("network.utils.admin.build")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!config.canBuild() && !(event.getPlayer().hasPermission("network.utils.admin.build") && event.getPlayer().getGameMode() == GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!config.canBuild() && !(event.getPlayer().hasPermission("network.utils.admin.build") && event.getPlayer().getGameMode() == GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (config.isInventoryLocked() && !event.getWhoClicked().hasPermission("network.utils.admin.inventory") && !event.getWhoClicked().getGameMode().equals(GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (config.isInventoryLocked() && !event.getPlayer().hasPermission("network.utils.admin.inventory") && !event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (config.isInventoryLocked() && !player.hasPermission("network.utils.admin.inventory") && !player.getGameMode().equals(GameMode.CREATIVE)) {
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (config.isInventoryLocked() && !player.hasPermission("network.utils.admin.inventory") && !player.getGameMode().equals(GameMode.CREATIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!config.canTakeDamage()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!config.canHunger()) {
            event.setCancelled(true);
        }
    }
}
