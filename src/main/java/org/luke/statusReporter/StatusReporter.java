package org.luke.statusReporter;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import org.luke.statusReporter.Command.CommandManager;
import org.luke.statusReporter.Placeholder.StatusExpansion;
import org.luke.statusReporter.WebSocket.WebSocketClient;

import java.net.URI;
public final class StatusReporter extends JavaPlugin {

    @Getter
    private static StatusReporter instance;

    public static String address_webServer;

    public record WebsocketInfo(String host, Integer port) {}

    @Getter
    @Setter
    private static WebsocketInfo websocketServerAddress;

    @Getter
    private static WebSocketClient webSocketClient;

    public enum ServerStatus {
        STARTING, // サーバーが起動中
        RUNNING, // 起動完了（動作中）
        SHUTTING_DOWN; // シャットダウン中
    }

    @Getter
    private static ServerStatus serverStatus = ServerStatus.STARTING;
    public static void setServerStatus(ServerStatus status) {
        serverStatus = status;
        Sender.Send();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        Load();

        Sender.Register();
        getServer().getPluginManager().registerEvents(new EventManager(), this);
        getServer().getPluginCommand("statusreporter").setExecutor(new CommandManager());

        getServer().getScheduler().runTask(this, () -> {
            // サーバーが完全に起動したらステータスを移行
            setServerStatus(ServerStatus.RUNNING);
            webSocketClient.sendMessageToServer(WebSocketClient.MessageType.STARTED, "");

            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new StatusExpansion().register();
            }
        });
    }

    public static void Websocket_ConnectToServer() {
        try {
            if(webSocketClient != null) {
                webSocketClient.close();
                webSocketClient = null;
            }
            StatusReporter.WebsocketInfo info = StatusReporter.getWebsocketServerAddress();
            URI serverUri = new URI(String.format("ws://%s:%d", info.host(), info.port()));
            webSocketClient = new WebSocketClient(serverUri);
            webSocketClient.setConnectionLostTimeout(300);
            webSocketClient.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }

        StatusReporter.setServerStatus(ServerStatus.SHUTTING_DOWN);
    }

    final String messageStatusPath = "messageStatus.";
    final String messageListPath = "messageStatus.message.";

    final String placeholderMessagePath = "placeholder-message.";
    public void Load() {
        FileConfiguration config = getConfig();
        address_webServer = config.getString("address");

        ConfigData configData = new ConfigData();

        //////////// messageStatus ////////////
        ConfigData.MessageStatus messageStatus = new ConfigData.MessageStatus();
        ConfigData.MessageData messageData = new ConfigData.MessageData();

        messageData.setStarting(config.getString(messageListPath + "starting"));
        messageData.setStarted(config.getString(messageListPath + "started"));
        messageData.setClosed(config.getString(messageListPath + "closed"));

        messageStatus.setMessageSwitch(config.getBoolean(messageStatusPath + "switch"));
        messageStatus.setFilterSwitch(config.getBoolean(messageStatusPath + "filter"));
        messageStatus.setFilter_servers(config.getStringList(messageStatusPath + "filter-list"));
        messageStatus.setMessageData(messageData);
        //////////// messageStatus ////////////

        //////////// PlaceholderMessage ////////////
        ConfigData.PlaceholderMessage placeholderMessage = new ConfigData.PlaceholderMessage();

        String online_path = placeholderMessagePath + "online.";
        String offline_path = placeholderMessagePath + "offline.";
        placeholderMessage.setStatus_online(config.getString(online_path + "status"));
        placeholderMessage.setStarting(config.getString(online_path + "starting"));
        placeholderMessage.setStatus_offline(config.getString(offline_path + "status"));
        //////////// PlaceholderMessage ////////////

        configData.setMessageStatus(messageStatus);
        configData.setPlaceholderMessage(placeholderMessage);
        data = configData;
    }
}