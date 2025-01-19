package org.luke.statusReporter.Placeholder;

import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.luke.statusReporter.API.getInfo;

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

        for(String serverName : status.keySet()) {
            if(serverName.equals(params)) {
                JSONObject serverStatus = status.getJSONObject(serverName);
                if(serverStatus.getBoolean("isOnline")) {
                    return switch(getInfo.getStatusByServerData(serverName).getStatus()) {
                        case RUNNING -> "稼働中";
                        case STARTING -> "起動中";
                        default -> "";
                    };
                } else {
                    return "オフラインです";
                }
            }
        }
        return "そのサーバーは存在しません。";
    }
}
