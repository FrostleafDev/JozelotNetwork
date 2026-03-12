package de.jozelot.jozelotArchive.utility;

import de.jozelot.jozelotArchive.JozelotArchive;
import org.bukkit.Bukkit;

public class PluginMessages {

    public static void sendStartup(JozelotArchive plugin) {
        var sender = Bukkit.getConsoleSender();
        var version = plugin.getDescription().getVersion();
        var mcVersion = Bukkit.getBukkitVersion();

       sender.sendMessage("§a[§JoArchive§a]§a Minecraft läuft in der " + mcVersion);
       sender.sendMessage("§a[§JoArchive§a]§a ----------------------------------------------");
       sender.sendMessage("§a[§JoArchive§a]§a    +==================+");
       sender.sendMessage("§a[§JoArchive§a]§a    |     JoArchive    |");
       sender.sendMessage("§a[§JoArchive§a]§a    +==================+");
       sender.sendMessage("§a[§JoArchive§a]§a ----------------------------------------------");
       sender.sendMessage("§a[§JoArchive§a]§6    Version: §e" +  version);
       sender.sendMessage("§a[§JoArchive§a]§a ----------------------------------------------");
       sender.sendMessage("§a[§JoArchive§a]§a Start war erfolgreich");
    }
}
