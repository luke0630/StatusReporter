package org.luke.statusReporter.JSON;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.luke.statusReporter.DynamicServerData;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class PlayersInfo {
    public static List<DynamicServerData.PlayerData> get() {
        List<DynamicServerData.PlayerData> playerList = new ArrayList<>();
        Server server = getServer();

        for(Player player : server.getOnlinePlayers()) {
            var info = new DynamicServerData.PlayerData();
            info.setName(player.getName());
            info.setUuid(String.valueOf(player.getUniqueId()));
            playerList.add(info);
        }

        return playerList;
    }
}
