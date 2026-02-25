package de.jozelot.jozelotLobby.player.settings;

import org.checkerframework.checker.units.qual.C;

public enum Setting {

    COLOR_PREFERENCE("color_preference", "WHITE");

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
