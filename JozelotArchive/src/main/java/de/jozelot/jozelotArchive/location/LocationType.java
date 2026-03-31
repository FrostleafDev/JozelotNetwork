package de.jozelot.jozelotArchive.location;

import org.bukkit.Material;

public enum LocationType {
    BASE("Base", Material.RED_BED),
    STARTER_BASE("Starter Base", Material.COBBLESTONE),
    MIDGAME_BASE("Midgame Base", Material.IRON_BLOCK),
    ENDGAME_BASE("Endgame Base", Material.RESPAWN_ANCHOR),
    FARM("Farm", Material.GOLDEN_HOE),
    SHOP("Shop", Material.DIAMOND),
    PUBLIC_LOCATION("Öffentliche Location", Material.COMPASS),
    POINT_OF_INTEREST("Point of Interest", Material.SPYGLASS),
    STRUCTURE("Struktur", Material.MOSSY_COBBLESTONE),
    COMMUNITY("Community Punkt", Material.CAMPFIRE),
    BIOME("Biom", Material.FLOWERING_AZALEA);

    private final String name;
    private final Material material;

    LocationType(String name, Material material) {
        this.name = name;
        this.material = material;
    }

    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
    }
}
