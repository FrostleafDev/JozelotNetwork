package de.jozelot.jozelotLobby.storage;

import de.jozelot.jozelotLobby.JozelotLobby;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final JozelotLobby plugin;

    private String redisHost;
    private String redisPassword;
    private int redisPort;

    private String colorPrimary;
    private String colorSecondary;
    private String colorTertiary;
    private String colorDanger;
    private String colorGrey;

    public ConfigManager(JozelotLobby plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public String getRedisHost() {
        return redisHost;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getColorGrey() {
        return colorGrey;
    }

    public String getColorDanger() {
        return colorDanger;
    }

    public String getColorTertiary() {
        return colorTertiary;
    }

    public String getColorSecondary() {
        return colorSecondary;
    }

    public String getColorPrimary() {
        return colorPrimary;
    }


    public void loadConfig() {
        plugin.saveDefaultConfig();

        redisHost = plugin.getConfig().getString("redis.host");
        redisPassword = plugin.getConfig().getString("redis.password");
        redisPort = plugin.getConfig().getInt("redis.port");

        colorPrimary = plugin.getConfig().getString("color_settings.primary");
        colorSecondary = plugin.getConfig().getString("color_settings.secondary");
        colorTertiary = plugin.getConfig().getString("color_settings.tertiary");
        colorDanger = plugin.getConfig().getString("color_settings.danger");
        colorGrey = plugin.getConfig().getString("color_settings.grey");
    }
}
