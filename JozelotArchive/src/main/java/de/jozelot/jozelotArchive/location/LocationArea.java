package de.jozelot.jozelotArchive.location;

import org.bukkit.World;

public class LocationArea {

    private double minX, minY, minZ;
    private double maxX, maxY, maxZ;
    private World world;

    public LocationArea(double x1, double y1, double z1, double x2, double y2, double z2, World world) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);

        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);

        this.world = world;
    }

    public boolean contains(org.bukkit.Location loc) {
        if (!loc.getWorld().equals(world)) {
            return false;
        }

        return loc.getX() >= minX && loc.getX() <= maxX &&
                loc.getY() >= minY && loc.getY() <= maxY &&
                loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
}
