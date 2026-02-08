package de.jozelot.jozelotUtils.storage;

import de.jozelot.jozelotUtils.JozelotUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LangManager {

    private final JozelotUtils plugin;
    private final ConfigManager config;
    private File langFile;
    private FileConfiguration langConfig;

    public LangManager(JozelotUtils plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    public void load() {
        langFile = new File(plugin.getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public void integrateRedisData(Map<String, String> redisData) {
        if (redisData == null) return;

        YamlConfiguration freshConfig = new YamlConfiguration();
        if (langFile.exists()) {
            try {
                freshConfig.load(langFile);
            } catch (Exception ignored) {}
        }

        for (Map.Entry<String, String> entry : redisData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value.contains("<<line>>")) {
                freshConfig.set(key, Arrays.asList(value.split("<<line>>")));
            } else {
                freshConfig.set(key, value);
            }
        }

        this.langConfig = freshConfig;

        //plugin.getLogger().info("Redis Daten integriert. Sound success: " + langConfig.getString("sounds.success"));
    }

    public String get(String path) {
        return langConfig.getString(path, "<red>Path not found: " + path);
    }

    public List<String> getStringList(String path) {
        return langConfig.getStringList(path);
    }

    public String format(String path, Map<String, String> variables) {
        return applyReplacements(get(path), path.equals("prefix"), variables);
    }


    public List<String> formatList(String path, Map<String, String> variables) {
        List<String> rawList = getStringList(path);
        List<String> formattedList = new ArrayList<>();

        for (String line : rawList) {
            formattedList.add(applyReplacements(line, false, variables));
        }
        return formattedList;
    }

    private String applyReplacements(String text, boolean isPrefix, Map<String, String> variables) {
        if (text == null || text.isEmpty()) return "";

        String msg = text;

        if (!isPrefix) {
            msg = msg.replace("{prefix}", get("prefix"));
        }

        msg = msg.replace("{primary}", "<" + config.getColorPrimary() + ">")
                .replace("{secondary}", "<" + config.getColorSecondary() + ">")
                .replace("{tertiary}", "<" + config.getColorTertiary() + ">")
                .replace("{danger}", "<" + config.getColorDanger() + ">")
                .replace("{grey}", "<" + config.getColorGrey() + ">")
                .replace("{plugin-version}", plugin.getDescription().getVersion());

        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return msg;
    }

    public String getRaw(String path) {
        return langConfig.getString(path);
    }
}