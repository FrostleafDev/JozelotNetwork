package de.jozelot.jozelotLobby.secrets.engine;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.secrets.objects.Secret;
import de.jozelot.jozelotLobby.secrets.objects.SecretRegion;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.*;

public class SecretManager {

    private final JozelotLobby plugin;
    private final SecretDatabase secretDb;
    private final MiniMessage mm = MiniMessage.miniMessage();


    public SecretManager(JozelotLobby plugin) {
        this.plugin = plugin;
        this.secretDb = plugin.getSecretDb();
        reload();
    }

    private List<Secret> secrets = new ArrayList<>();

    public void reload() {
        secrets.clear();
        secrets.addAll(secretDb.loadAllSecrets());
    }

    public void checkLocation(Player player, Location loc) {
        for (Secret secret : secrets) {
            if (secret.isInside(loc)) {
                handelDiscovery(player, secret);
                break;
            }
        }
    }

    private void handelDiscovery(Player player, Secret secret) {
        LobbyPlayer lp = plugin.getLobbyPlayerManager().getPlayer(player);
        if (lp == null) return;

        if (lp.hasFoundSecret(secret.getId())) return;

        lp.addFoundSecret(secret.getId());
        int foundSecrets = lp.getFoundSecretIds().size();
        int maxSecrets = secrets.size();

        player.sendActionBar(mm.deserialize(plugin.getLang().format("secret-found-actionbar", Map.of("secret-name", secret.getName()))));
        List<String> message = plugin.getLang().formatList("secret-found-message", Map.of("secret-name", secret.getName(), "secrets-found", String.valueOf(foundSecrets), "secrets-max", String.valueOf(maxSecrets), "progress-bar", createProgressBar(foundSecrets)));
        player.sendMessage(mm.deserialize(String.join("<newline>", message)));
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getUniqueId() != player.getUniqueId())
                .forEach(p ->
                p.sendMessage(mm.deserialize(plugin.getLang().format("secret-found-message-other", Map.of("player-name", player.getName(), "current-secrets", String.valueOf(foundSecrets), "max-secrets", String.valueOf(maxSecrets))))));

        player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.UI, 1, 1);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getRedisSetup().getJedis().publish("network:secrets", player.getUniqueId() + ":" + foundSecrets + ":" + maxSecrets);
        });
    }

    public int addSecret(String name, String description, String materialName, Location loc1, Location loc2) {
        SecretRegion region = new SecretRegion(loc1.toVector(), loc2.toVector(), loc1.getWorld().getName());
        int id = secretDb.createSecret(name, description, materialName, region);
        if (id != -1) {
            reload();
            notifyProxyOfGlobalChange();
        }
        return id;
    }

    public void removeSecret(int id) {
        secretDb.deleteSecret(id);
        reload();
        notifyProxyOfGlobalChange();
        plugin.getLogger().info("Secret mit ID " + id + " wurde gelöscht.");
    }

    public List<Secret> getSecrets() {
        return secrets;
    }

    public Location[] getWorldEditSelection(Player player) {
        try {
            var actor = BukkitAdapter.adapt(player);
            var session = WorldEdit.getInstance().getSessionManager().get(actor);
            var region = session.getSelection(actor.getWorld());

            if (region == null) return null;

            return new Location[]{
                    BukkitAdapter.adapt(player.getWorld(), region.getMinimumPoint()),
                    BukkitAdapter.adapt(player.getWorld(), region.getMaximumPoint())
            };
        } catch (Exception e) {
            return null;
        }
    }

    private String createProgressBar(int secretsFound) {
        StringBuilder bar = new StringBuilder("[");

        int secretsMax = secrets.size();
        int blocks = 10;

        int filledBlocks = (int) Math.round((double) secretsFound / secretsMax * 10);

        for (int i = 0; i < filledBlocks; i++) {
            bar.append("■");
        }
        for (int i = filledBlocks; i < blocks; i++) {
            bar.append("□");
        }
        bar.append("]");

        return bar.toString();
    }

    private void notifyProxyOfGlobalChange() {
        int maxSecrets = secrets.size();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getRedisSetup().getJedis().publish("network:secrets", "GLOBAL_UPDATE:0:" + maxSecrets);
        });
    }


}
