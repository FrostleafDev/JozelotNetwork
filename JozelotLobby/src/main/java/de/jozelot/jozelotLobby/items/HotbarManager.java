package de.jozelot.jozelotLobby.items;

import de.jozelot.jozelotLobby.JozelotLobby;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class HotbarManager {

    private JozelotLobby plugin;

    public HotbarManager(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    public void clearHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, null);
        }
    }

    public void giveItems(Player player) {
        
    }


}
