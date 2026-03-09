package de.jozelot.jozelotProxy.apis;

import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class PteroManager {
    private final String url;
    private final String key;
    private final ConfigManager config;
    private final JozelotProxy plugin;

    public PteroManager(JozelotProxy plugin) {
        this.config = plugin.getConfig();
        this.plugin = plugin;
        this.url = config.getString("pterodactyl.url");
        this.key = config.getString("pterodactyl.api-key");
    }

    /**
     * For the /net restart/start/stop commands
     * @param pteroId The ID from the db in the pterodactyl panel.
     * @param action Start/Stop/Restart
     * @param callback
     */
    public void sendAction(String pteroId, String action, java.util.function.Consumer<Integer> callback) {
        if (pteroId == null || pteroId.isEmpty()) {
            callback.accept(404);
            return;
        }

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url + "/api/client/servers/" + pteroId + "/power"))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .header("Accept", "Application/vnd.pterodactyl.v1+json")
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString("{\"signal\": \"" + action + "\"}"))
                .build();

        client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> callback.accept(res.statusCode()));
    }

    public void getResources(String pteroId, java.util.function.Consumer<com.google.gson.JsonObject> callback) {
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url + "/api/client/servers/" + pteroId + "/resources"))
                .header("Authorization", "Bearer " + key)
                .header("Accept", "Application/vnd.pterodactyl.v1+json")
                .GET()
                .build();

        client.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() == 200) {
                        com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(res.body()).getAsJsonObject();
                        callback.accept(json.getAsJsonObject("attributes"));
                    } else {
                        callback.accept(null);
                    }
                });
    }

    public void deleteFiles(String pteroId, List<String> filesToDelete) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"root\": \"/\", \"files\": [");
        for (int i = 0; i < filesToDelete.size(); i++) {
            jsonBuilder.append("\"").append(filesToDelete.get(i)).append("\"");
            if (i < filesToDelete.size() - 1) jsonBuilder.append(",");
        }
        jsonBuilder.append("]}");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + "/api/client/servers/" + pteroId + "/files/delete"))
                .header("Authorization", "Bearer " + key)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBuilder.toString()))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(res -> {
                    if (res.statusCode() != 204) {
                        plugin.getConsoleLogger().broadCastToConsole("<red>[PteroAPI] Fehler beim Loeschen: " + res.statusCode() + " - " + res.body());
                    } else {
                        plugin.getConsoleLogger().broadCastToConsole("<green>[PteroAPI] Dateien fuer " + pteroId + " geloescht.");
                    }
                });
    }
}
