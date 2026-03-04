package de.jozelot.jozelotUtils.listener;

import de.jozelot.jozelotUtils.JozelotUtils;
import de.jozelot.jozelotUtils.storage.ConfigManager;
import de.jozelot.jozelotUtils.storage.LangManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class FallOffListener implements Listener {

    private final ConfigManager config;
    private final LangManager lang;

    public FallOffListener(JozelotUtils plugin) {
        this.config = plugin.getConfigManager();
        this.lang = plugin.getLang();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!config.isSpawnOnFall()) {
            //event.getPlayer().sendMessage("Spawnfall ist aus");
            return;
        }

        if (event.getTo().getY() == event.getFrom().getY()) {
            return;
        }

        if (event.getTo().getY() <= config.getFallOffHeight()) {
            //event.getPlayer().sendMessage("unter der höhe");
            if (config.getSpawnLocation() != null) {
                //event.getPlayer().sendMessage("teleport");
                Player player = event.getPlayer();
                player.teleport(config.getSpawnLocation());
                player.setFallDistance(0);
                playSound(event.getPlayer(), "success");
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!config.isSpawnOnJoin()) {
            //event.getPlayer().sendMessage("spawn join ist aus");
            return;
        }
        Player player = event.getPlayer();

        if (config.getSpawnLocation() != null) {
            //event.getPlayer().sendMessage("teleport");
            player.teleport(config.getSpawnLocation());
            player.setFallDistance(0);
        }
    }

    private void playSound(CommandSender sender, String soundKey) {
        if (!(sender instanceof Player player)) return;

        String path = lang.getRaw("sounds." + soundKey);

        if (path != null && !path.isEmpty()) {
            try {
                String cleanedPath = path.trim().toLowerCase();

                if (!cleanedPath.contains(":")) {
                    cleanedPath = "minecraft:" + cleanedPath;
                }

                Sound sound = Sound.sound(
                        Key.key(cleanedPath),
                        Sound.Source.MASTER,
                        1.0f,
                        1.0f
                );
                player.playSound(sound);
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("§cUngültiger Sound-Key in lang.yml: " + path);
            }
        }
    }
}
