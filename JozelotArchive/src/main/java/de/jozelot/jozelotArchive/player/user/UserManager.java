package de.jozelot.jozelotArchive.player.user;

import de.jozelot.jozelotArchive.JozelotArchive;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class UserManager {

    private final JozelotArchive plugin;
    private final Map<UUID, User> users = new HashMap<>();

    public UserManager(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public User registerUser(Player player) {
        User user = new User(player.getUniqueId(), plugin);
        users.put(player.getUniqueId(), user);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            user.setSettings(plugin.getServiceManager().getUserDatabase().getAllSettings(user));
        });
        return user;
    }

    public void removeAllUsers() {
        // TODO: Save to Database
        users.clear();
    }

    public void registerAllUsers() {
        // TODO: Load from Database
        Bukkit.getOnlinePlayers().forEach(p -> registerUser(p));
    }

    public void removeUser(UUID uuid) {
        users.remove(uuid);
    }

    public void removeUser(User user) {
        removeUser(user.getUniqueId());
    }

    public void removeUser(Player player) {
        removeUser(player.getUniqueId());
    }

    public User getUser(UUID uuid) {
        return users.get(uuid);
    }

    public User getUser(Player player) {
        return getUser(player.getUniqueId());
    }

    public Map<UUID, User> getUsersAsMap() {
        return Collections.unmodifiableMap(users);
    }

    public Collection<User> getUsersAsCollection() {
        return Collections.unmodifiableCollection(users.values());
    }
}
