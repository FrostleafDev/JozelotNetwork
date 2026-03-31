package de.jozelot.jozelotArchive.inventory.menus.navigator.locations;

import de.jozelot.jozelotArchive.player.user.settings.ColorPreference;

import javax.annotation.Nullable;

public enum LocationSort {
    NAME("Name"),
    MEMBERS("Mitglieder"),
    TYPE("Type"),
    SIZE("Größe");

    private final String name;

    LocationSort(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public static LocationSort getByName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public LocationSort next() {
        LocationSort[] values = values();
        int nextOrdinal = (this.ordinal() + 1) % values.length;
        return values[nextOrdinal];
    }
}
