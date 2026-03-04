package de.jozelot.jozelotLobby.secrets.engine;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import de.jozelot.jozelotLobby.JozelotLobby;
import de.jozelot.jozelotLobby.player.LobbyPlayer;
import de.jozelot.jozelotLobby.secrets.objects.Secret;
import de.jozelot.jozelotLobby.secrets.objects.SecretRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SecretManager {

    private final JozelotLobby plugin;
    private final SecretDatabase secretDb;

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

        player.sendMessage("§a§lSecret gefunden!");
        player.sendMessage("§7Du hast §e" + secret.getName() + " §7entdeckt.");

        lp.playSound("success");
    }

    public int addSecret(String name, String description, String materialName, Location loc1, Location loc2) {
        SecretRegion region = new SecretRegion(loc1.toVector(), loc2.toVector(), loc1.getWorld().getName());
        int id = secretDb.createSecret(name, description, materialName, region);
        if (id != -1) reload();
        return id;
    }

    public void removeSecret(int id) {
        secretDb.deleteSecret(id);

        reload();
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
}
