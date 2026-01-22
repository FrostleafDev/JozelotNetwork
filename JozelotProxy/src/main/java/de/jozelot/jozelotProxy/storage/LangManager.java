package de.jozelot.jozelotProxy.storage;

import de.jozelot.jozelotProxy.JozelotProxy;
import org.yaml.snakeyaml.Yaml;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LangManager {

    private final File file;
    private Map<String, Object> data;
    private final Yaml yaml;
    private final ConfigManager config;

    private String prefix;
    private String onlyPlayer;
    private String noPermission;

    private String networkReloadSuccess;

    public LangManager(JozelotProxy plugin, Path directory, String fileName) {
        this.file = new File(directory.toFile(), fileName);
        this.yaml = new Yaml();
        this.config = plugin.getConfig();

        if (!directory.toFile().exists()) {
            directory.toFile().mkdirs();
        }
    }

    public String getOnlyPlayer() {
        return onlyPlayer;
    }

    public void loadDefaultLang(InputStream defaultStream) {
        if (!file.exists()) {
            try {
                Files.copy(defaultStream, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        reload();
    }

    public String getNoPermission() {
        return noPermission;
    }

    public String getNetworkReloadSuccess() {
        return networkReloadSuccess;
    }

    public void reload() {
        try (InputStream in = new FileInputStream(file)) {
            this.data = yaml.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        prefix = getString("prefix");
        onlyPlayer = format("only-player", null);
        noPermission = format("no-permission", null);
        networkReloadSuccess = format("network-reload-success", null);
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Method to change variables in the lang.yml with {} to variables from the map
     * @param path The path from the lang.yml. e.g: 'command-ban-usage'
     * @param variables Map of the variables to replace. First one which text should be replaced. Second one with what it should be replaced with
     * @return
     */
    public String format(String path, Map<String, String> variables) {
        return applyReplacements(getString(path), path.equals("prefix"), variables);
    }

    /**
     * Same as from the format method but for lists
     * @param path The path from the lang.yml. e.g: 'command-ban-kick'
     * @param variables Map of the variables to replace. First one which text should be replaced. Second one with what it should be replaced with
     * @return
     */
    public List<String> formatList(String path, Map<String, String> variables) {
        List<String> rawList = getStringList(path);
        List<String> formattedList = new ArrayList<>();

        for (String line : rawList) {
            formattedList.add(applyReplacements(line, false, variables));
        }

        return formattedList;
    }

    /**
     * This is the place where the whole thing gets replaced
     * @param isPrefix
     * @param text
     * @param variables
     * @return
     */
    private String applyReplacements(String text, boolean isPrefix, Map<String, String> variables) {
        if (text == null || text.isEmpty()) return "";

        String msg = text;

        if (!isPrefix && getPrefix() != null) {
            msg = msg.replace("{prefix}", getPrefix());
        }

        msg = msg.replace("{primary}", "<" + config.getColorPrimary() + ">")
                .replace("{secondary}", "<" + config.getColorSecondary() + ">")
                .replace("{tertiary}", "<" + config.getColorTertiary() + ">")
                .replace("{danger}", "<" + config.getColorDanger() + ">")
                .replace("{grey}", "<" + config.getColorGrey() + ">");

        // Eigene Variablen
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return msg;
    }

    // Die Methode für config.getString("pfad.zum.wert")
    @SuppressWarnings("unchecked")
    public String getString(String path) {
        Object o = get(path);
        return o != null ? o.toString() : "";
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String path) {
        Object o = get(path);
        if (o instanceof List) {
            return (List<String>) o;
        }
        return new ArrayList<>();
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