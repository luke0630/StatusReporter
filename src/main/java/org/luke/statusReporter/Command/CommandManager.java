package org.luke.statusReporter.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.luke.statusReporter.API.getInfo;
import org.luke.statusReporter.Data.DynamicServerData;
import org.luke.statusReporter.Sender;

import java.util.List;

import static org.luke.takoyakiLibrary.TakoUtility.toColor;

public class CommandManager implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if(strings[0].equals("reconnect")) {
            if(commandSender instanceof Player player) {
                player.sendMessage(toColor("&c接続を再試行しています。 詳細はコンソールを確認してください。"));
            }
            Sender.Register();
        }
        if(strings[0].equals("list")) {
            List<DynamicServerData> list = getInfo.getStatusList();
            for(DynamicServerData serverData : list) {
                commandSender.sendMessage(serverData.getServerName());
            }
        } else {
            String jsonResponse = getInfo.getStatusJSON();
            if (jsonResponse != null) {
                commandSender.sendMessage(jsonResponse);
            } else {
                commandSender.sendMessage("Failed to fetch server status.");
            }
        }
        return true;
    }
}
