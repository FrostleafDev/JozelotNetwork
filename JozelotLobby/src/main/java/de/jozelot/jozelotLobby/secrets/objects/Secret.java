package de.jozelot.jozelotLobby.secrets.objects;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class Secret {

    private final int id;
    private final String name;
    private final String description;
    private final Material icon;
    private final List<SecretRegion> regions;

    public Secret(int id, String name, String description, String block) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = Material.matchMaterial(block) != null
                ? Material.matchMaterial(block)
                : Material.BARRIER;
        this.regions = new ArrayList<>();
    }

    public boolean isInside(Location loc) {
        return regions.stream().anyMatch(region -> region.contains(loc));
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    public List<SecretRegion> getRegions() {
        return regions;
    }
}
