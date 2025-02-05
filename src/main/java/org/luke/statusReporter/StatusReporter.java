package org.luke.statusReporter;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import org.json.JSONObject;
import org.luke.statusReporter.Command.CommandManager;
import org.luke.statusReporter.Data.ConfigData;
import org.luke.statusReporter.Placeholder.StatusExpansion;
import org.luke.statusReporter.WebSocket.MyWebsocketClient;
import org.manager.Library.Data.DynamicServerData;
import org.manager.Library.Data.Message.MessageType;

import java.net.URI;
import java.util.Objects;

public final class StatusReporter extends JavaPlugin {
    @Getter
    private static ConfigData data;

    @Getter
    private static StatusReporter instance;
    public static String address_webServer;
    public record WebsocketInfo(String host, Integer port) {}

    @Getter
    @Setter
    private static WebsocketInfo websocketServerAddress;

    @Getter
    private static MyWebsocketClient webSocketClient;

    @Setter
    @Getter
    private static DynamicServerData.ServerStatus serverStatus = DynamicServerData.ServerStatus.STARTING;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveDefaultConfig();
        Load();

        Sender.Register();
        getServer().getPluginManager().registerEvents(new EventManager(), this);
        Objects.requireNonNull(getServer().getPluginCommand("statusreporter")).setExecutor(new CommandManager());

        getServer().getScheduler().runTask(this, () -> {
            setServerStatus(DynamicServerData.ServerStatus.RUNNING);
            if(webSocketClient != null) {
                webSocketClient.sendMessage(MessageType.MessageClient.STARTED, new JSONObject());
                webSocketClient.SendInfo();
            }

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
            webSocketClient = new MyWebsocketClient(serverUri);
            webSocketClient.setConnectionLostTimeout(300);
            webSocketClient.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        if(webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
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

        placeholderMessage.setStarting(config.getString(placeholderMessagePath + "starting"));

        String online_path = placeholderMessagePath + "online.";
        String offline_path = placeholderMessagePath + "offline.";
        placeholderMessage.setStatus_online(config.getString(online_path + "status"));
        placeholderMessage.setStatus_offline(config.getString(offline_path + "status"));

        placeholderMessage.setOnline_playerscount(config.getString(online_path + "playerscount"));
        placeholderMessage.setOnline_version(config.getString(online_path + "version"));

        //////////// PlaceholderMessage ////////////

        configData.setMessageStatus(messageStatus);
        configData.setPlaceholderMessage(placeholderMessage);
        data = configData;
    }
}