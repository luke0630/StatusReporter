package org.luke.statusReporter;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.json.JSONObject;
import org.luke.statusReporter.JSON.PlayersInfo;
import org.luke.statusReporter.JSON.PluginsInfo;
import org.luke.statusReporter.WebSocket.WebSocketClient;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.bukkit.Bukkit.getPort;
import static org.bukkit.Bukkit.getServer;

public class Sender {
    private static String myServerName = "";

    // 再接続を試みるメソッド
    private static void attemptReconnect() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(StatusReporter.getInstance(), () -> {
            System.out.println("接続を再試行します");
            Register();
        }, (long) (1.5 * 20L));
    }

    public static void Register() {
        System.out.println("接続します");
        String url = String.format("http://%s/register", StatusReporter.address_webServer);

        JSONObject serverJSON = new JSONObject();

        serverJSON.put("port", getPort());

        String jsonPayload = serverJSON.toString();
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            getServer().getLogger().info(String.format(
                    "%s  %s",
                    response.statusCode(),
                    response.body()
            ));

            JSONObject jsonObject = new JSONObject(response.body());
            String address = jsonObject.getString("host");
            Integer port = jsonObject.getInt("port");
            myServerName = jsonObject.getString("name");

            StatusReporter.WebsocketInfo info = new StatusReporter.WebsocketInfo(
                    address,
                    port
            );

            StatusReporter.setWebsocketServerAddress(info);
            WebSocketClient.tryConnect();
        } catch (IOException | InterruptedException e) {
            attemptReconnect();
        }
    }

    public static void Send() {
        String url = String.format("http://%s/status", StatusReporter.address_webServer);

        Gson gson = new Gson();
        DynamicServerData resultData = new DynamicServerData();
        resultData.setServerName(myServerName);
        resultData.setPlugins(PluginsInfo.get());
        resultData.setPlayers(PlayersInfo.get());

        String jsonPayload = gson.toJson(resultData);
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Server-Name", myServerName)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            getServer().getLogger().info(String.format(
                    "%s  %s",
                    response.statusCode(),
                    response.body()
            ));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error sending request", e);
        }
    }
}
