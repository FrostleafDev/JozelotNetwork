package de.jozelot.jozelotArchive.player.user;

import de.jozelot.jozelotArchive.JozelotArchive;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {

    private final JozelotArchive plugin;
    private Map<UUID, User> users = new HashMap<>();

    public UserManager(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public User registerUser(Player player) {
        User user = new User(player.getUniqueId(), plugin);
        users.put(player.getUniqueId(), user);
        return user;
    }

    public void removeAllUsers() {
        users.clear();
    }

    public void registerAllUsers() {
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
}
