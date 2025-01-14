package org.luke.statusReporter;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import org.luke.statusReporter.API.getInfo;

public final class StatusReporter extends JavaPlugin {

    @Getter
    private static StatusReporter instance;

    public static String address_webServer;

    public record WebsocketInfo(String host, Integer port) {}

    @Getter
    @Setter
    private static WebsocketInfo websocketServerAddress;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        saveDefaultConfig();

        address_webServer = getConfig().getString("address");

        Sender.Register();
        getServer().getPluginManager().registerEvents(new EventManager(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
