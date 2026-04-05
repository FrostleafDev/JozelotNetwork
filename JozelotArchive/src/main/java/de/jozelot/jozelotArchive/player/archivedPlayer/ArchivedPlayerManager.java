package de.jozelot.jozelotArchive.player.archivedPlayer;

import de.jozelot.jozelotArchive.JozelotArchive;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArchivedPlayerManager {

    private final JozelotArchive plugin;
    private final Map<UUID, ArchivedPlayer> players = new HashMap<>();

    public ArchivedPlayerManager(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public ArchivedPlayer registerPlayer(UUID uuid, String name, String skinValue, String skinSignature,
                                         ItemStack[] inventory, ItemStack[] armor, ItemStack[] enderChest,
                                         String worldName, double x, double y, double z, float yaw, float pitch) {

        ArchivedPlayer player = new ArchivedPlayer(uuid, name, skinValue, skinSignature,
                inventory, armor, enderChest,
                worldName, x, y, z, yaw, pitch);
        players.put(uuid, player);
        return player;
    }

    public ArchivedPlayer loadFromDatFile(UUID uuid, String name) {
        Path file = Paths.get("world/playerdata_old", uuid.toString() + ".dat");
        if (!Files.exists(file)) return null;

        try {
            // Wir nutzen den internen Bukkit-Weg, um NBT zu lesen
            // Das gibt uns ein "NBTTagCompound" (als Map-Ersatz)
            var nbt = Bukkit.getUnsafe().loadItemRaw(Files.readAllBytes(file));

            // Da 'loadItemRaw' eigentlich für Items ist, müssen wir tricksen.
            // Falls das bei deiner Paper-Version nicht geht, hier der sicherere Weg:

            // Wir laden die Datei als ConfigurationSection (Bukkit kann das!)
            // Aber Achtung: Das funktioniert nur, wenn die Datei NICHT komprimiert ist.
            // Da Minecraft-Daten komprimiert sind, kommen wir um NMS oder Library
            // eigentlich nicht herum, WENN wir die Datei direkt anfassen.
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}