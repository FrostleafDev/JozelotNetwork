package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.InventoryHolder;

public class GriefPrevention implements Listener {

    private final ConfigManager config;
    private JozelotUtils plugin;

    public GriefPrevention(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
        this.plugin = plugin;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (plugin.getVanishManager().isVanished(player.getUniqueId())) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block != null) {
                Material type = block.getType();

                if (type == Material.CHEST || type == Material.TRAPPED_CHEST ||
                        type == Material.BARREL || type.name().contains("SHULKER_BOX") ||
                        type == Material.ENDER_CHEST) {

                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setCancelled(true);

                    if (type == Material.ENDER_CHEST) {
                        player.openInventory(player.getEnderChest());
                    } else {
                        BlockState state = block.getState();
                        if (state instanceof Container) {
                            Container container = (Container) state;
                            player.openInventory(container.getInventory());
                        }
                    }
                    return;
                }
            }
        }

        if (canBypass(player)) return;

        if (!config.canBuild()) {
            event.setCancelled(true);
        }
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

        if (event.getDamager() instanceof Player player) {
            if (!canBypass(player)) {
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
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (config.isInventoryLocked()) {
            boolean isAuthorizedAdmin = (player.getGameMode() == GameMode.CREATIVE)
                    && player.hasPermission("network.utils.admin.build");
            if (!isAuthorizedAdmin) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (config.isInventoryLocked()) {
            boolean isAuthorizedAdmin = (player.getGameMode() == GameMode.CREATIVE)
                    && player.hasPermission("network.utils.admin.build");
            if (!isAuthorizedAdmin) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (config.isInventoryLocked()) {
            boolean isAuthorizedAdmin = (player.getGameMode() == GameMode.CREATIVE)
                    && player.hasPermission("network.utils.admin.build");
            if (!isAuthorizedAdmin) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (config.isInventoryLocked()) {
            boolean isAuthorizedAdmin = (player.getGameMode() == GameMode.CREATIVE)
                    && player.hasPermission("network.utils.admin.build");
            if (!isAuthorizedAdmin) {
                event.setCancelled(true);
            }
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
        if (config.isCanMobSpawn()) return;

        CreatureSpawnEvent.SpawnReason reason = event.getEntity().getEntitySpawnReason();

        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG || reason == CreatureSpawnEvent.SpawnReason.COMMAND || reason == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }

        if (event.getEntity() instanceof org.bukkit.entity.LivingEntity && !(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        if (config.isBlockPortals()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (config.isBlockPortals()) {
            event.setCancelled(true);
        }
    }
}