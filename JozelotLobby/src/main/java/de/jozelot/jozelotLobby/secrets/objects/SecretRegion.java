package de.jozelot.jozelotLobby.secrets.objects;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class SecretRegion {

    private final Vector min;
    private final Vector max;
    private final String worldName;

    public SecretRegion(Vector v1, Vector v2, String worldName) {
        this.min = Vector.getMinimum(v1, v2);
        this.max = Vector.getMaximum(v1, v2);
        this.worldName = worldName;
    }

    public boolean contains(Location loc) {
        if (!loc.getWorld().getName().equals(worldName)) return false;
        return loc.toVector().isInAABB(min, max);
    }

    public Vector getMin() {
        return min;
    }

    public Vector getMax() {
        return max;
    }

    public String getWorldName() {
        return worldName;
    }
}