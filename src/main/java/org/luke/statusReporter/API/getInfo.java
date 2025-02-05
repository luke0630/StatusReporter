package org.luke.statusReporter.API;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;
import org.json.JSONObject;
import org.luke.statusReporter.StatusReporter;
import org.manager.Library.Data.DynamicServerData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@UtilityClass
public class getInfo {
    public CompletableFuture<String> getStatusJSON() {
        HttpClient client = HttpClient.newHttpClient();

        String url = String.format("http://%s/api/status", StatusReporter.address_webServer);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        String responseBody = response.body();
                        if (responseBody == null || responseBody.isEmpty()) {
                            throw new IllegalStateException("Empty response body received.");
                        }
                        return responseBody;
                    } else {
                        throw new RuntimeException("HTTP Error: " + response.statusCode());
                    }
                })
                .exceptionally(e -> {
                    System.err.println(e.getMessage());
                    return "{}";
                });
    }

    public CompletableFuture<List<DynamicServerData>> getStatusList() {
        return getStatusJSON().thenApply(json -> {
            List<DynamicServerData> list = new ArrayList<>();

            if (json != null && !json.isEmpty()) {
                JSONObject status = new JSONObject(json);

                for (String key : status.keySet()) {
                    JSONObject serverStatus = status.getJSONObject(key);
                    JSONObject serverData = serverStatus.getJSONObject("serverData");

                    Gson gson = new Gson();
                    DynamicServerData data = gson.fromJson(serverData.toString(), DynamicServerData.class);
                    list.add(data);
                }
            }
            return list;
        });
    }

    public CompletableFuture<DynamicServerData> getStatusByServerData(String serverName) {
        getStatusJSON().thenApply(string_status -> {
            if(string_status != null) {
                JSONObject status = new JSONObject(string_status);

                for(String key :  status.keySet()) {
                    JSONObject json_serverData = status.getJSONObject(key);
                    JSONObject serverData = json_serverData.getJSONObject("serverData");
                    if(serverData.getString("serverName").equals(serverName)) {
                        Gson gson = new Gson();
                        return gson.fromJson(serverData.toString(), DynamicServerData.class);
                    }
                }
            }
            return null;
        });

         return null;
    }
}

