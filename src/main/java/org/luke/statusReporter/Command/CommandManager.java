package org.luke.statusReporter.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.luke.statusReporter.API.getInfo;
import org.luke.statusReporter.Data.DynamicServerData;

import java.util.Objects;

public class CommandManager implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if(strings.length <= 0) return true;
        switch(strings[0]) {
            case "list" -> {
                getInfo.getStatusList().thenAccept(dynamicServerData -> {
                    for(DynamicServerData serverData : dynamicServerData) {
                        commandSender.sendMessage(serverData.getServerName());
                    }
                });
            }

            case "status" -> {
                getInfo.getStatusJSON().thenAccept(jsonResponse -> {
                    commandSender.sendMessage(Objects.requireNonNullElse(jsonResponse, "Failed to fetch server status."));
                });
            }
            default -> commandSender.sendMessage("そのコマンドは存在しません");
        }
        return true;
    }
}
