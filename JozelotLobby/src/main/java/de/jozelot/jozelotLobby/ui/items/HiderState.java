package de.jozelot.jozelotLobby.ui.items;

public enum HiderState {
    VISIBLE,
    TEAM,
    HIDDEN;

    public HiderState next() {
        // ordinal() -> gibt die Position (0, 1, 2) zurück
        int nextIndex = (this.ordinal() + 1) % values().length;
        return values()[nextIndex];
    }
}
