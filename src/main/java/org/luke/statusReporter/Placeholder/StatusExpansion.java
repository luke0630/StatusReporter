package org.luke.statusReporter.Placeholder;

import com.google.gson.Gson;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.luke.statusReporter.API.getInfo;
import org.luke.statusReporter.Data.DynamicServerData;
import org.luke.statusReporter.StatusReporter;

public class StatusExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "status";
    }

    @Override
    public @NotNull String getAuthor() {
        return "luke0630";
    }

    @Override
    public @NotNull String getVersion() {
        return "";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        JSONObject status = new JSONObject(getInfo.getStatusJSON());

        String[] args = params.split("_");
        for(String serverName : status.keySet()) {
            if(serverName.equals(args[0])) {
                JSONObject serverStatus = status.getJSONObject(serverName);
                if(serverStatus.getBoolean("isOnline")) {
                    DynamicServerData serverData = new Gson().fromJson(serverStatus.getJSONObject("serverData").toString(), DynamicServerData.class);
                    return getStatus(serverData, args);
                } else if(args.length == 1) {
                    return StatusReporter.getData().getPlaceholderMessage().getStatus_offline();
                } else {
                    return "";
                }
            }
        }
        return "そのサーバーは存在しません。";
    }

    private String getStatus(DynamicServerData serverStatus, String[] args) {
        if(args.length == 1) {
            return switch(serverStatus.getStatus()) {
                case RUNNING -> StatusReporter.getData().getPlaceholderMessage().getStatus_online();
                case STARTING -> StatusReporter.getData().getPlaceholderMessage().getStarting();
                default -> "";
            };
        } else if(args.length == 2) {
            return switch(args[1]) {
                case "playerscount" -> String.valueOf(serverStatus.getPlayers().size());
                case "version" -> serverStatus.getVersion();
                default -> "";
            };
        }
        return "";
    }
}
