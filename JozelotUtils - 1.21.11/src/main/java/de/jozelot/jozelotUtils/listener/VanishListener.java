package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null || !plugin.getVanishManager().isVanished(player.getUniqueId())) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (block.getState() instanceof Container container) {
            // 1. Wir verhindern die Standard-Aktion komplett
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setCancelled(true);

            if (block.getType() == Material.ENDER_CHEST) {
                player.openInventory(player.getEnderChest());
                return;
            }

            // 2. Der entscheidende Trick für 1.21.1:
            // Wir erstellen ein temporäres Inventar, das direkt auf das echte zugreift,
            // aber keinen Block-Holder hat.
            Inventory realInv = container.getInventory();
            Inventory silentInv = Bukkit.createInventory(null, realInv.getSize(),
                    container.getCustomName() != null ? container.getCustomName() : container.getType().name());

            // Wir spiegeln den Inhalt
            silentInv.setContents(realInv.getContents());

            // 3. Wir öffnen das lautlose Inventar
            player.openInventory(silentInv);

            // 4. Synchronisation beim Schließen sicherstellen (siehe unten)
            player.setMetadata("vanish_silent_container", new FixedMetadataValue(plugin, block.getLocation()));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player) || !player.hasMetadata("vanish_silent_container")) return;

        Location loc = (Location) player.getMetadata("vanish_silent_container").get(0).value();
        player.removeMetadata("vanish_silent_container", plugin);

        if (loc != null && loc.getBlock().getState() instanceof Container container) {
            // Inhalt vom lautlosen Inventar zurück in das echte Inventar schreiben
            container.getInventory().setContents(event.getInventory().getContents());
            container.update();
        }
    }
}