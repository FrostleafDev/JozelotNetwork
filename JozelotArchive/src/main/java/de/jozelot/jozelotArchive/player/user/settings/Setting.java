package de.jozelot.jozelotArchive.player.user.settings;

import de.jozelot.jozelotArchive.inventory.menus.navigator.player.PlayerSort;

public enum Setting {

    COLOR_PREFERENCE("color_preference", "WHITE"),
    PLAYER_SORT("player_sort", "NAME"),
    LOCATION_SORT("location_sort", "NAME");

    private final String key;
    private final String defaultValue;

    Setting(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() { return key; }
    public String getDefaultValue() { return defaultValue; }

    public static Setting fromKey(String key) {
        for (Setting setting : values()) {
            if (setting.getKey().equalsIgnoreCase(key)) {
                return setting;
            }
        }
        return null;
    }
}
