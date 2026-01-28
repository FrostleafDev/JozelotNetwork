package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class GriefPrevention implements Listener {

    private final ConfigManager config;

    public GriefPrevention(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
    }

    private boolean canBypass(Player player) {
        return player.hasPermission("network.utils.admin.build") && player.getGameMode() == GameMode.CREATIVE;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.canBuild() && !canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!config.canBuild() && !canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (config.canBuild()) return;
        if (canBypass(event.getPlayer())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (config.canBuild()) return;
        if (canBypass(event.getPlayer())) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (config.canBuild()) return;

        EntityType type = event.getEntityType();
        if (type == EntityType.ITEM_FRAME || type == EntityType.ARMOR_STAND || type == EntityType.GLOW_ITEM_FRAME) {
            if (event.getDamager() instanceof Player player) {
                if (!canBypass(player)) event.setCancelled(true);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!config.canBuild() && !canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!config.canBuild() && !canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (config.isInventoryLocked() && !canBypass(player) && !player.hasPermission("network.utils.admin.inventory")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (config.isInventoryLocked() && !canBypass(player) && !player.hasPermission("network.utils.admin.inventory")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (config.isInventoryLocked() && !canBypass(player) && !player.hasPermission("network.utils.admin.inventory")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent event) {
        if (config.isInventoryLocked() && !canBypass(event.getPlayer()) && !event.getPlayer().hasPermission("network.utils.admin.inventory")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!config.canTakeDamage() && event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!config.canHunger()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityFocus(EntityTargetEvent event) {
        if (!config.isEntitiesFocusPlayer() && event.getTarget() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!config.isEntityGrief()) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onEndermanGrief(EntityChangeBlockEvent event) {
        if (!config.isEntityGrief() && event.getEntityType() == EntityType.ENDERMAN) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (config.canBuild()) return;

        if (event.getEntity().getShooter() instanceof Player player) {
            if (!canBypass(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onArmorStandInteract(PlayerArmorStandManipulateEvent event) {
        if (!config.canBuild() && !canBypass(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        if (!config.isCanMobSpawn()) {
            if (event.getEntity() instanceof org.bukkit.entity.LivingEntity && !(event.getEntity() instanceof Player)) {
                event.setCancelled(true);
            }
        }
    }
}