package de.jozelot.jozelotLobby.ui.lobbyHeads;

import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class LobbyHeadListener implements Listener {

    private final JozelotLobby plugin;
    private final LobbyHeadManager headManager;

    public LobbyHeadListener(JozelotLobby plugin, LobbyHeadManager headManager) {
        this.plugin = plugin;
        this.headManager = headManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            LobbyPlayer lobbyPlayer = plugin.getLobbyPlayerManager().getPlayer(event.getPlayer().getUniqueId());

            if (block == null) return;

            Location loc = block.getLocation();
            LobbyHead head = headManager.getHeadAt(loc);

            if (head != null) {
                event.setCancelled(true);

                Player player = event.getPlayer();

                lobbyPlayer.playSound("pling");

                player.sendMessage(MiniMessage.miniMessage().deserialize(head.getText()));
            }
        }
    }
}