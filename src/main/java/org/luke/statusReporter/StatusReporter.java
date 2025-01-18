package org.luke.statusReporter;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.luke.statusReporter.API.getInfo;
import org.luke.statusReporter.Command.CommandManager;
import org.luke.statusReporter.Placeholder.StatusExpansion;
import org.luke.statusReporter.WebSocket.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

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

        getServer().getScheduler().runTask(this, () -> {
            // サーバーが完全に起動したらRUNNING
            setServerStatus(ServerStatus.RUNNING);
            webSocketClient.sendMessageToServer(WebSocketClient.MessageType.STARTED, "");

            if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                new StatusExpansion().register();
            }
        });

        saveDefaultConfig();

        address_webServer = getConfig().getString("address");

        getLogger().info(address_webServer);

        Sender.Register();
        getServer().getPluginManager().registerEvents(new EventManager(), this);
        getServer().getPluginCommand("statusreporter").setExecutor(new CommandManager());
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
}
