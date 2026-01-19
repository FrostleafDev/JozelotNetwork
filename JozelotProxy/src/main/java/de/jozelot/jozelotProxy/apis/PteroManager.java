package de.jozelot.jozelotProxy.apis;

import de.jozelot.jozelotProxy.JozelotProxy;
import de.jozelot.jozelotProxy.storage.ConfigManager;

public class PteroManager {
    private final String url;
    private final String key;
    private final ConfigManager config;

    public PteroManager(JozelotProxy plugin) {
        this.config = plugin.getConfig();
        this.url = config.getString("pterodactyl.url");
        this.key = config.getString("pterodactyl.api-key");
    }

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
}
