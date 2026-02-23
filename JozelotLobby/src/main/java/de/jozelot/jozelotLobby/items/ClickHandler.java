package de.jozelot.jozelotLobby.items;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.player.PlayerConnectionListener;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ClickHandler implements Listener {

    private final JozelotLobby plugin;

    public ClickHandler(JozelotLobby plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        Player player = event.getPlayer();

        if (itemStack == null || !itemStack.hasItemMeta() || player.hasCooldown(itemStack.getType())) return;

        ItemMeta itemMeta = itemStack.getItemMeta();
        String itemId = itemMeta.getPersistentDataContainer().get(HotbarItems.ITEM_ID, PersistentDataType.STRING);

        if (itemId == null) return;

        LobbyPlayer lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(player);

        switch (itemId) {
            case "navigator":

                break;
            case "profile":

                break;
            case "player_hider":
                if (lobbyPlayer == null) {
                    break;
                }
                lobbyPlayer.toggleHider();
                plugin.getHotbarManager().giveItems(player);
                player.setCooldown(itemStack, 20);
        }

        lobbyPlayer.playSound("pling");

        event.setCancelled(true);
    }
}
