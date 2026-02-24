package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class JoinListener implements Listener {

    private final ConfigManager config;
    private final LangManager lang;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final JozelotUtils plugin;

    public JoinListener(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean isVanished = plugin.getVanishManager().isVanished(player.getUniqueId());

        if (isVanished) {
            event.joinMessage(null);
        } else {
            handleJoinMessage(event, player);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getVanishManager().updatePlayerVanish(player);

            if (!isVanished) {
                player.setInvulnerable(false);
                player.setGlowing(false);
                player.setCollidable(true);
            }
        }, 1L);

        // 2. Alle anderen versteckten Spieler für den neuen Spieler ausblenden
        plugin.getVanishManager().updateAllForPlayer(player);

        // 3. Spielmodus setzen
        try {
            GameMode gm = GameMode.valueOf(config.getDefaultGamemode().toUpperCase());
            player.setGameMode(gm);
        } catch (IllegalArgumentException e) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        // 4. Flugmodus (verzögert wegen Teleportation/Join-Lags)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if ((player.hasPermission("network.utils.join.fly") && config.isAutomaticFlight()) || config.isAutomaticFlightPlayer()) {
                enableFly(player);
            } else {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }, 2L);

        player.setFlySpeed(0.1f);

        // 5. XP & Hotbar
        if (config.isCustomExperienceLevel()) {
            player.setLevel(config.getCustomExperienceLevel());
        }
        if (config.isCustomBarLevel()) {
            player.setExp(config.getCustomBarLevel() / 100.0f);
        }
        if (config.getDefaultHotbarSlot() != -1) {
            player.getInventory().setHeldItemSlot(config.getDefaultHotbarSlot());
        }
    }

    private void handleJoinMessage(PlayerJoinEvent event, Player player) {
        if (config.getJoinMessageType().equalsIgnoreCase("disabled")) {
            event.joinMessage(null);
        } else if (config.getJoinMessageType().equalsIgnoreCase("custom")) {
            event.joinMessage(null); // Standardnachricht weg
            String msg = lang.format("join-message", Map.of("player-name", player.getName()));
            if (msg != null && !msg.isEmpty()) {
                Bukkit.broadcast(mm.deserialize(msg));
            }
        }
        // Bei "default" lassen wir Bukkit die Nachricht einfach schicken
    }

    private void enableFly(Player player) {
        player.setAllowFlight(true);
        player.setFlying(true);
        if (player.isOnGround()) {
            player.teleport(player.getLocation().add(0, 0.1, 0));
        }
    }
}