package de.jozelot.jozelotProxy.storage;

import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ConfigManager {

    private final File file;
    private Map<String, Object> data;
    private final Path faviconDirectory;
    private final Yaml yaml;

    private String colorPrimary;
    private String colorSecondary;
    private String colorTertiary;
    private String colorDanger;
    private String colorGrey;

    private String lobbyServer;

    private String mysqlHost;
    private String mysqlDatabase;
    private String mysqlUser;
    private String mysqlPassword;
    private int mysqlPort;

    private String redisHost;
    private String redisPassword;
    private int redisPort;

    public ConfigManager(Path directory, String fileName) {
        this.file = new File(directory.toFile(), fileName);
        this.faviconDirectory = directory.resolve("favicons");
        this.yaml = new Yaml();

        if (!directory.toFile().exists()) {
            directory.toFile().mkdirs();
        }
        if (!faviconDirectory.toFile().exists()) {
            faviconDirectory.toFile().mkdirs();
        }
    }

    public void loadDefaultConfig(InputStream defaultStream) {
        if (!file.exists()) {
            try {
                Files.copy(defaultStream, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        reload();
    }

    public String getColorPrimary() {
        return colorPrimary;
    }

    public String getColorSecondary() {
        return colorSecondary;
    }

    public String getColorTertiary() {
        return colorTertiary;
    }

    public String getColorDanger() {
        return colorDanger;
    }

    public String getColorGrey() {
        return colorGrey;
    }

    public String getLobbyServer() {
        return lobbyServer;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public String getMysqlHost() {
        return mysqlHost;
    }

    public String getMysqlDatabase() {
        return mysqlDatabase;
    }

    public String getMysqlUser() {
        return mysqlUser;
    }

    public String getMysqlPassword() {
        return mysqlPassword;
    }

    public int getMysqlPort() {
        return mysqlPort;
    }

    public void reload() {
        try (InputStream in = new FileInputStream(file)) {
            this.data = yaml.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        lobbyServer = getString("lobby-server");

        redisHost = getString("redis.host");
        redisPort = getInt("redis.port");
        redisPassword = getString("redis.password");

        mysqlHost = getString("mysql.host");
        mysqlDatabase = getString("mysql.database");
        mysqlUser = getString("mysql.user");
        mysqlPassword = getString("mysql.password");
        mysqlPort = getInt("mysql.port");

        colorPrimary = getString("color-settings.primary");
        colorSecondary = getString("color-settings.secondary");
        colorTertiary = getString("color-settings.tertiary");
        colorDanger = getString("color-settings.danger");
        colorGrey = getString("color-settings.grey");
    }

    // Die Methode für config.getString("pfad.zum.wert")
    @SuppressWarnings("unchecked")
    public String getString(String path) {
        Object o = get(path);
        return o != null ? o.toString() : "";
    }

    public int getInt(String path) {
        Object o = get(path);
        return o instanceof Number ? ((Number) o).intValue() : 0;
    }

    public long getLong(String path) {
        Object o = get(path);
        return o instanceof Number ? ((Number) o).longValue() : 0L;
    }

    public boolean getBoolean(String path) {
        Object o = get(path);
        return o instanceof Boolean ? (Boolean) o : false;
    }

    public double getDouble(String path) {
        Object o = get(path);
        return o instanceof Number ? ((Number) o).doubleValue() : 0.0;
    }

    public Path getFaviconDirectory() {
        return faviconDirectory;
    }

    // Hilfsmethode für verschachtelte Pfade (Punkt-Logik)
    private Object get(String path) {
        if (data == null) return null;
        String[] keys = path.split("\\.");
        Object current = data;

        for (String key : keys) {
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(key);
        }
        return current;
    }


}