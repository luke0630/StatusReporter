package org.luke.statusReporter.WebSocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.luke.statusReporter.StatusReporter;

import java.net.URI;

public class MyWebSocketClient extends WebSocketClient {

    public MyWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("Connected to server");

        // サーバーにメッセージを送信
        send("register test");
    }

    @Override
    public void onMessage(String message) {
        // サーバーから受け取ったメッセージ
        System.out.println("Received from server: " + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Closed connection: " + reason);

        attemptReconnect();
    }

    @Override
    public void onError(Exception ex) {

    }

    // 再接続を試みるメソッド
    private void attemptReconnect() {
        new Thread(() -> {
            try {
                // 再接続の前に一定の待機時間を設ける
                Thread.sleep((long) (1.5 * 1000));
                System.out.println("接続を再試行します");
                tryConnect();
            } catch (InterruptedException e) {
                System.out.println("cannot connect");
            }
        }).start();
    }

    public static void tryConnect() {
        try {
            StatusReporter.WebsocketInfo info = StatusReporter.getWebsocketServerInfo();
            URI serverUri = new URI(String.format("ws://%s:%d", info.host(), info.port()));
            MyWebSocketClient client = new MyWebSocketClient(serverUri);

            client.connect();
        } catch (Exception e) {
            System.out.println("cannot connect");
        }
    }
}
