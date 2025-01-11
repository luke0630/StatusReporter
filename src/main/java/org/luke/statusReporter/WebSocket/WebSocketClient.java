package org.luke.statusReporter.WebSocket;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.luke.statusReporter.Sender;
import org.luke.statusReporter.StatusReporter;

import java.net.URI;
import java.net.URISyntaxException;

import static org.bukkit.Bukkit.getServer;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    static final double reconnectDuration = 1.5; // second

    public WebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        // 接続成功時にスケジューラーを停止
        if (scheduler != null) {
            scheduler.cancel();
            scheduler = null; // スケジューラーをクリア
        }
        System.out.println("サーバーに接続しました");

        // サーバーにメッセージを送信
        JSONObject json = new JSONObject();
        json.put("port", getServer().getPort());
        send(json.toString());
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received from server: " + message);
        if(message.equals("send")) {
            Sender.Send();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Closed connection: " + reason);

        attemptReconnect();
    }

    @Override
    public void onError(Exception ex) {

    }

    private static BukkitTask scheduler = null;

    // 再接続を試みるメソッド
    private static void attemptReconnect() {
        if(scheduler != null) return;
        scheduler = Bukkit.getScheduler().runTaskLaterAsynchronously(StatusReporter.getInstance(), () -> {
            System.out.println("接続を再試行します");
            tryConnect();
            scheduler.cancel();
            scheduler = null;
        }, (long) (reconnectDuration * 20L));
    }

    public static void tryConnect() {
        try {
            StatusReporter.WebsocketInfo info = StatusReporter.getWebsocketServerAddress();
            URI serverUri = new URI(String.format("ws://%s:%d", info.host(), info.port()));
            WebSocketClient client = new WebSocketClient(serverUri);
            client.setConnectionLostTimeout(300);
            client.connect();
        } catch (URISyntaxException e) {
            attemptReconnect();
        }
    }
}