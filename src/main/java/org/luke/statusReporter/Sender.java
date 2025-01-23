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
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.*;

public class Sender {
    private static String myServerName = "";
    static final Integer timeout = 5;
    private static HttpClient client = null;

    static Logger logger;
    static boolean reconnecting = false;

    public static void Register() {
        logger = StatusReporter.getInstance().getLogger();
        if(!reconnecting) {
            logger.info("接続しています");
        }
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
                        reconnecting = false;
                        logger.info("接続しました。");
                        logger.info(
                                String.format(
                                        "%s  %s",
                                        response.statusCode(),
                                        response.body()
                                )
                        );

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
                    } catch (Exception e) {
                        logger.warning("非同期タスクでエラーが発生しました: " + e.getMessage());
                    }
                })
                .exceptionally(ex -> {
                    if(!reconnecting) {
                        logger.warning("----------StatusReporter----------");
                        logger.warning("サーバーに接続できませんでした。詳細: " + ex.getMessage());
                        logger.warning("接続先: " + url);
                        logger.warning(timeout + "秒ごとに接続を試みます。");
                        logger.warning("----------StatusReporter----------");
                    }
                    Bukkit.getScheduler().runTaskLaterAsynchronously(StatusReporter.getInstance(), () -> {
                        reconnecting = true;
                        Sender.Register();
                    },timeout * 20L);
                    return null;
                });
    }


    public static void Send() {
        String url = String.format("http://%s/status", StatusReporter.address_webServer);
        isServerOnline(url, timeout).thenAccept(isOnline -> {
            if(!isOnline) return;
            try {
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

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json; charset=UTF-8")
                        .header("Server-Name", myServerName)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .timeout(Duration.ofSeconds(10))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                getLogger().info("サーバーに接続できませんでした。オフラインの可能性があります。 接続を試みる場合/status reconnect を使用してください  接続先: " + url);
                Register();
            } catch (InterruptedException e) {
                getLogger().info("リクエストが中断されました");
                getLogger().info("詳細: " + e);
                Register();
            }
        });
    }

    public static CompletableFuture<Boolean> isServerOnline(String url, int timeoutSeconds) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int statusCode = response.statusCode();
                    return statusCode >= 200 && statusCode < 400;
                })
                .exceptionally(e -> false);
    }
}
