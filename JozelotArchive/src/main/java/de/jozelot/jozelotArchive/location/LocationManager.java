package de.jozelot.jozelotArchive.location;

import com.sun.jna.platform.win32.Winspool;
import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.player.archivedPlayer.ArchivedPlayer;
import de.jozelot.jozelotArchive.player.user.User;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.lang.foreign.PaddingLayout;
import java.util.*;

public class LocationManager {

    private final JozelotArchive plugin;
    private final Map<Integer, Location> locations = new HashMap<>();

    public LocationManager(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public void loadLocations() {
        locations.clear();

        List<Location> fromDb = plugin.getServiceManager().getLocationDatabase().getAllLocations();

        for (Location loc : fromDb) {
            Set<UUID> memberUuids = plugin.getServiceManager().getLocationDatabase().loadMemberUUIDs(loc.getId());

            locations.put(loc.getId(), loc);
        }

        plugin.getLogger().info("Es wurden " + locations.size() + " Locations aus der Datenbank geladen.");
    }

    public void saveLocations() {
        if (locations.isEmpty()) return;

        for (Location loc : locations.values()) {
            plugin.getServiceManager().getLocationDatabase().saveLocation(loc);
        }
        plugin.getLogger().info(locations.size() + " Locations wurden synchronisiert.");
    }

    public int createLocation(@NotNull LocationType type, @NotNull String name, String description, ArchivedPlayer owner, Set<ArchivedPlayer> members, @NotNull LocationArea area) {
        Location location = new Location(0, type, name, description, owner, members, area);

        int generatedId = plugin.getServiceManager().getLocationDatabase().saveLocation(location);

        if (generatedId != -1) {
            location.setId(generatedId);
            locations.put(generatedId, location);
        }

        return generatedId;
    }

    public void removeLocation(int id) {
        locations.remove(id);
    }

    public void removeLocation(Location location) {
        locations.remove(location.getId());
    }

    public Map<Integer, Location> getLocationsAsMap() {
        return Collections.unmodifiableMap(locations);
    }

    public Collection<Location> getLocationsAsCollection() {
        return Collections.unmodifiableCollection(locations.values());
    }

    public int getLocationCount() {
        return locations.size();
    }
}
