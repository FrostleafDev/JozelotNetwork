package de.jozelot.jozelotArchive.location;

import com.sun.jna.platform.win32.Winspool;
import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.player.archivedPlayer.ArchivedPlayer;
import de.jozelot.jozelotArchive.player.user.User;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.PaddingLayout;
import java.util.*;

public class LocationManager {

    private final JozelotArchive plugin;
    private final Map<Integer, Location> locations = new HashMap<>();

    public LocationManager(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public void createLocation(@NotNull LocationType type, @NotNull String name, String description, ArchivedPlayer owner, Set<ArchivedPlayer> members, @NotNull LocationArea area) {
       // Location location = new Location()
    }

    public Map<Integer, Location> getLocationsAsMap() {
        return Collections.unmodifiableMap(locations);
    }

    public Collection<Location> getLocationsAsCollection() {
        return Collections.unmodifiableCollection(locations.values());
    }
}
