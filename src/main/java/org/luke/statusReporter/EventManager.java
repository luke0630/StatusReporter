package org.luke.statusReporter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static org.bukkit.Bukkit.getServer;
import static org.luke.statusReporter.StatusReporter.*;

public class EventManager implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getServer().getScheduler().runTask(getInstance(), StatusReporter.getWebSocketClient()::SendInfo);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        getServer().getScheduler().runTask(getInstance(), StatusReporter.getWebSocketClient()::SendInfo);
    }
}
