package de.jozelot.jozelotArchive.inventory.menus.navigator.player;

import de.jozelot.jozelotArchive.player.user.settings.ColorPreference;

import javax.annotation.Nullable;

public enum PlayerSort {
    NAME("Name"),
    PLAYTIME("Spielzeit"),
    DEATHS("Tode"),
    KILLS("Kills"),
    BLOCKS_PLACED("Blöcke plaziert"),
    BLOCKS_BROKEN("Blöcke abgebaut");

    private final String name;

    PlayerSort(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public static PlayerSort getByName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public PlayerSort next() {
        PlayerSort[] values = values();
        int nextOrdinal = (this.ordinal() + 1) % values.length;
        return values[nextOrdinal];
    }
}
