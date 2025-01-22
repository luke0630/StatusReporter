package org.luke.statusReporter;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.json.JSONObject;
import org.luke.statusReporter.Data.DynamicServerData;
import org.luke.statusReporter.JSON.PlayersInfo;
import org.luke.statusReporter.JSON.PluginsInfo;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.bukkit.Bukkit.*;

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
        System.out.println("接続しています");
        String url = String.format("http://%s/register", StatusReporter.address_webServer);

        JSONObject serverJSON = new JSONObject();
        serverJSON.put("port", getPort());

        String jsonPayload = serverJSON.toString();
        if(client == null) {
            client = HttpClient.newHttpClient();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(timeout))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAcceptAsync(response -> {
                    try {
                        Bukkit.getScheduler().runTask(StatusReporter.getInstance(), () -> {
                            getServer().getLogger().info(String.format(
                                    "%s  %s",
                                    response.statusCode(),
                                    response.body()
                            ));
                        });

            JSONObject jsonObject = new JSONObject(response.body());
            String address = jsonObject.getString("host");
            Integer port = jsonObject.getInt("port");
            myServerName = jsonObject.getString("name");

            StatusReporter.WebsocketInfo info = new StatusReporter.WebsocketInfo(
                    address,
                    port
            );

            StatusReporter.setWebsocketServerAddress(info);
            StatusReporter.Websocket_ConnectToServer();
        } catch (IOException e) {
            getLogger().info("サーバーに接続できませんでした。オフラインの可能性があります。 接続を試みる場合/status reconnect を使用してください  接続先: " + url);
        } catch (InterruptedException e) {
            getLogger().info("リクエストが中断されました");
            getLogger().info("詳細: " + e);
            attemptReconnect();
        }
    }


    public static void Send() {
        String url = String.format("http://%s/status", StatusReporter.address_webServer);

        Gson gson = new Gson();
        DynamicServerData resultData = new DynamicServerData();
        resultData.setServerName(myServerName);
        resultData.setStatus(StatusReporter.getServerStatus());
        resultData.setPlugins(PluginsInfo.get());
        resultData.setPlayers(PlayersInfo.get());

        String[] split = StatusReporter.getInstance().getServer().getBukkitVersion().split("-");
        String version = split[0];
        resultData.setVersion(version);

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
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error sending request", e);
        }
    }
}
