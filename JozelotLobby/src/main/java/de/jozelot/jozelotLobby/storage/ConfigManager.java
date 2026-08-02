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

    private String mysqlHost;
    private String mysqlUser;
    private String mysqlPassword;
    private String mysqlDatabase;
    private int mysqlPort;

    private boolean eventServerActive;

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


    public int getMysqlPort() {
        return mysqlPort;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public String getMysqlUser() {
        return mysqlUser;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public boolean isEventServerActive() {
        return eventServerActive;
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

        mysqlDatabase = plugin.getConfig().getString("mysql.database");
        mysqlPassword = plugin.getConfig().getString("mysql.password");
        mysqlUser = plugin.getConfig().getString("mysql.user");
        mysqlHost = plugin.getConfig().getString("mysql.host");
        mysqlPort = plugin.getConfig().getInt("mysql.port");

        eventServerActive = plugin.getConfig().getBoolean("items.event_server.active", false);
    }
}
