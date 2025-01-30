package org.luke.statusReporter.WebSocket;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.luke.statusReporter.Data.ConfigData;
import org.luke.statusReporter.Data.DynamicServerData;
import org.luke.statusReporter.Data.Message.MessageDataServer;
import org.luke.statusReporter.Data.Message.MessageType;
import org.luke.statusReporter.JSON.PlayersInfo;
import org.luke.statusReporter.JSON.PluginsInfo;
import org.luke.statusReporter.Sender;
import org.luke.statusReporter.StatusReporter;
import org.luke.statusReporter.Utility.MessageUtility;
import org.luke.takoyakiLibrary.TakoUtility;

import java.net.URI;

import static org.bukkit.Bukkit.getServer;
import static org.luke.statusReporter.Sender.myServerName;

public class MyWebsocketClient extends org.java_websocket.client.WebSocketClient {
    public MyWebsocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        StatusReporter.getInstance().getLogger().info("websocketサーバーに接続しました。");
        sendMessage(MessageType.MessageClient.REGISTER,
                new JSONObject().put("port", StatusReporter.getInstance().getServer().getPort())
        );
    }

    @Override
    public void onMessage(String message) {
        MessageDataServer messageDataServer = new Gson().fromJson(message, MessageDataServer.class);
        MessageType.MessageServer type = messageDataServer.type;

        switch(messageDataServer.type) {
            case REGISTER_RESULT:
                // 認証されました
                JSONObject jsonObject = new JSONObject(messageDataServer.content);
                myServerName = jsonObject.getString("name");
                SendInfo();
                break;
            case SEND_INFO :
                SendInfo();
                break;
            case UPDATE_INFO:
                // データが更新された
                break;
            case UPDATE_CLOSED:
            case UPDATE_REGISTERED :
            case UPDATE_STARTED:
                ConfigData.MessageStatus MessageStatus = StatusReporter.getData().getMessageStatus();

                if(!MessageStatus.getMessageSwitch()) return;
                JSONObject content = new JSONObject(messageDataServer.content);
                String serverName = content.getString("name");
                String serverDisplayName = content.getString("displayName");

                // フィルターが有効
                if(MessageStatus.getFilterSwitch()) {
                    // フィルターリストにサーバーの名前が存在しなかった場合メッセージを送信しません
                    if(!MessageStatus.getFilter_servers().contains(serverName)) return;
                }

                String statusMessage = getStatusMessage(type);
                getServer().broadcastMessage(TakoUtility.toColor(String.format(statusMessage, serverDisplayName)));
        }
    }

    String getStatusMessage(MessageType.MessageServer type) {
        ConfigData.MessageData messageData = StatusReporter.getData().getMessageStatus().getMessageData();
        if(type == MessageType.MessageServer.UPDATE_STARTED) {
            for(Player player : Bukkit.getOnlinePlayers()) {
                Bukkit.getScheduler().runTask(StatusReporter.getInstance(), () -> player.getWorld().playSound(
                        player.getLocation(),
                        Sound.ENTITY_PLAYER_LEVELUP,
                        1,
                        1
                ));
            }
        }
        return switch(type) {
            case UPDATE_REGISTERED -> messageData.getStarting();
            case UPDATE_STARTED -> messageData.getStarted();
            case UPDATE_CLOSED -> messageData.getClosed();
            default -> "";
        };
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        // サーバーが理由でcloseした場合Registerを実行
        if(remote) {
            StatusReporter.getInstance().getLogger().info("サーバーとの接続が切断されました 理由: " + reason);
            if(code != 1000) {
                Sender.Register();
            }
        }
    }

    @Override
    public void onError(Exception ex) {

    }

    public void sendMessage(MessageType.MessageClient type, JSONObject message) {
        if (isOpen()) {
            send(
                MessageUtility.getResultResponse(type, message)
            );
        } else {
            StatusReporter.getInstance().getLogger().info("Connection is not open. Unable to send message.");
        }
    }

    public void SendInfo() {
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

        sendMessage(MessageType.MessageClient.SEND_INFO, new JSONObject(jsonPayload));
    }
}