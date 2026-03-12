package de.jozelot.jozelotArchive.player.archivedPlayer;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ArchivedPlayer {

    private final UUID uuid;
    private final String name;

    private final String skinValue;
    private final String skinSignature;

    private final ItemStack[] inventory;
    private final ItemStack[] armor;
    private final ItemStack[] enderChest;

    private final String worldName;
    private final double x, y, z;
    private final float yaw, pitch;

    public ArchivedPlayer(UUID uuid, String name, String skinValue, String skinSignature,
                          ItemStack[] inventory, ItemStack[] armor, ItemStack[] enderChest,
                          String worldName, double x, double y, double z, float yaw, float pitch) {
        this.uuid = uuid;
        this.name = name;
        this.skinValue = skinValue;
        this.skinSignature = skinSignature;
        this.inventory = inventory;
        this.armor = armor;
        this.enderChest = enderChest;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }


    private void spawn() {

    }

    private void despawn() {

    }
}
