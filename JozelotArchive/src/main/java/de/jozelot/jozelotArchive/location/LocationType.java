package de.jozelot.jozelotArchive.location;

public enum LocationType {
    BASE("Base"),
    STARTER_BASE("Starter Base"),
    MIDGAME_BASE("Midgame Base"),
    ENDGAME_BASE("Endgame Base"),
    FARM("Farm"),
    SHOP("Shop"),
    PUBLIC_LOCATION("Öffentliche Location"),
    POINT_OF_INTEREST("Point of Interest"),
    STRUCTURE("Struktur"),
    COMMUNITY("Community Punkt"),
    BIOME("Biom");

    private final String name;

    LocationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
