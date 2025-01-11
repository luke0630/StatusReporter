package org.luke.statusReporter;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static org.bukkit.Bukkit.getServer;

public class EventManager implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getServer().getScheduler().runTask(StatusReporter.getInstance(), Sender::Send);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        getServer().getScheduler().runTask(StatusReporter.getInstance(), Sender::Send);
    }
}
