package org.luke.statusReporter.Command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.luke.statusReporter.API.getInfo;
import org.luke.statusReporter.DynamicServerData;
import org.luke.statusReporter.StatusReporter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandManager implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        CompletableFuture.runAsync(() -> {
            if(strings[0].equals("list")) {
                List<DynamicServerData> list = getInfo.getStatusList();
                for(DynamicServerData serverData : list) {
                    commandSender.sendMessage(serverData.getServerName());
                }
            } else {
                String jsonResponse = getInfo.getStatusJSON();
                if (jsonResponse != null) {
                    // メインスレッドでメッセージを送信
                    Bukkit.getScheduler().runTask(StatusReporter.getInstance(), () -> {
                        commandSender.sendMessage(jsonResponse);
                    });
                } else {
                    Bukkit.getScheduler().runTask(StatusReporter.getInstance(), () -> {
                        commandSender.sendMessage("Failed to fetch server status.");
                    });
                }
            }
        });
        return true;
    }
}
