package de.jozelot.jozelotArchive.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ContainerOpenListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        Material type = block.getType();

        if (block.getState() instanceof Container container) {
            player.openInventory(container.getInventory());
            return;
        }

        switch (type) {
            case CRAFTING_TABLE -> player.openWorkbench(block.getLocation(), true);
            case LOOM -> player.openLoom(block.getLocation(), true);
            case STONECUTTER -> player.openStonecutter(block.getLocation(), true);
            case SMITHING_TABLE -> player.openSmithingTable(block.getLocation(), true);
            case GRINDSTONE -> player.openGrindstone(block.getLocation(), true);
            case ENCHANTING_TABLE -> player.openEnchanting(block.getLocation(), true);
            case CARTOGRAPHY_TABLE -> player.openCartographyTable(block.getLocation(), true);
            case ANVIL, CHIPPED_ANVIL, DAMAGED_ANVIL -> player.openAnvil(block.getLocation(), true);
        }
    }
}
