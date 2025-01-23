package org.luke.statusReporter.WebSocket;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.luke.statusReporter.Data.ConfigData;
import org.luke.statusReporter.Sender;
import org.luke.statusReporter.StatusReporter;
import org.luke.takoyakiLibrary.TakoUtility;

import java.net.URI;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    public enum MessageType {
        REGISTER,
        STARTED, // 起動済み
        CLOSED,
    }

    public WebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        StatusReporter.getInstance().getLogger().info("websocketサーバーに接続しました。");

        Register();
    }

    @Override
    public void onMessage(String message) {
        String[] split_message = message.split(" ");
        if(split_message.length >= 2) {
            ConfigData.MessageStatus MessageStatus = StatusReporter.getData().getMessageStatus();

            if(!MessageStatus.getMessageSwitch()) return;
            MessageType type = MessageType.valueOf(split_message[0]);
            String serverName = split_message[1];
            String serverDisplayName = split_message[2];

            // フィルターが有効
            if(MessageStatus.getFilterSwitch()) {
                if(!MessageStatus.getFilter_servers().contains(serverName)) return; // フィルターリストにサーバーの名前が存在しなかった場合メッセージを送信しません
            }

            String statusMessage = getStatusMessage(type);
            getServer().broadcastMessage(TakoUtility.toColor(String.format(statusMessage, serverDisplayName)));
        } else if(message.equals("send")) {
            Sender.Send();
        }
    }

    String getStatusMessage(MessageType type) {
        ConfigData.MessageData messageData = StatusReporter.getData().getMessageStatus().getMessageData();
        if(type == MessageType.STARTED) {
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
        }
        return switch(type) {
            case REGISTER -> messageData.getStarting();
            case STARTED -> messageData.getStarted();
            case CLOSED -> messageData.getClosed();
        };
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        StatusReporter.getInstance().getLogger().info("Closed connection: " + reason);

        Sender.Register();
    }

    @Override
    public void onError(Exception ex) {

    }
    public void sendMessageToServer(MessageType type, String message) {
        if (isOpen()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", type.name());
            jsonObject.put("content", message);
            getLogger().info(type + " " + message);
            send(jsonObject.toString());
        } else {
            StatusReporter.getInstance().getLogger().info("Connection is not open. Unable to send message.");
        }
    }

    public void Register() {
        JSONObject json = new JSONObject();
        json.put("port", getServer().getPort());
        sendMessageToServer(MessageType.REGISTER, json.toString());
    }
}