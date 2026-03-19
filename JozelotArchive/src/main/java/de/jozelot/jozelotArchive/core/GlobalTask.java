package de.jozelot.jozelotArchive.core;

import de.jozelot.jozelotArchive.JozelotArchive;
import de.jozelot.jozelotArchive.player.user.User;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class GlobalTask {

    private final JozelotArchive plugin;
    private BukkitTask task;

    public GlobalTask(JozelotArchive plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) return;

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            /*for (User user : plugin.getServiceManager().getUserManager().getUsers().values()) {
                user.sendGameModeActionBar();
            }*/

        }, 20L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }
}
