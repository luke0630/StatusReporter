package org.luke.statusReporter.WebSocket;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.luke.statusReporter.Sender;
import org.luke.statusReporter.StatusReporter;

import java.net.URI;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    public enum MessageType {
        REGISTER,
        STARTED, // 起動済み
        CLOSED,
    }
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

        Register();
    }

    @Override
    public void onMessage(String message) {
        String[] split_message = message.split(" ");
        if(split_message.length == 2) {
            switch(MessageType.valueOf(split_message[0])) {
                case REGISTER -> {
                    getServer().broadcastMessage(split_message[1] + " が起動中です");
                }
                case STARTED -> {
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        Bukkit.getScheduler().runTask(StatusReporter.getInstance(), () -> {
                            player.getWorld().playSound(
                                    player.getLocation(),
                                    Sound.ENTITY_PLAYER_LEVELUP,
                                    1,
                                    1
                            );
                        });
                    }
                    getServer().broadcastMessage(split_message[1] + " が起動しました。");
                }
                case CLOSED -> {
                    getServer().broadcastMessage(split_message[1] + " が終了しました。");
                }
            }
        }
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
            StatusReporter.Websocket_ConnectToServer();
            scheduler.cancel();
            scheduler = null;
        }, (long) (reconnectDuration * 20L));
    }

    public void sendMessageToServer(MessageType type, String message) {
        if (isOpen()) {
            getLogger().info(type + " " + message);
            send(String.format("%s %s",
                    type.name(),
                    message
            ));
        } else {
            System.out.println("Connection is not open. Unable to send message.");
        }
    }

    public void Register() {
        JSONObject json = new JSONObject();
        json.put("port", getServer().getPort());
        sendMessageToServer(MessageType.REGISTER, json.toString());
    }
}