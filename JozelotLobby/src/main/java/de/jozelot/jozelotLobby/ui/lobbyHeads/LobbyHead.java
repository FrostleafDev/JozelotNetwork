package de.jozelot.jozelotLobby.ui.lobbyHeads;

import org.bukkit.Location;

public class LobbyHead {

    private final int x, y, z;
    private final String text;

    public LobbyHead(int x, int y, int z, String text) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.text = text;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getText() {
        return text;
    }

    public boolean isAt(Location loc) {
        return loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z;
    }
}