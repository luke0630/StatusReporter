package org.luke.statusReporter.Placeholder;

import com.google.gson.Gson;
import org.bukkit.OfflinePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.luke.statusReporter.API.getInfo;
import org.luke.statusReporter.StatusReporter;
import org.manager.Library.Data.DynamicServerData;

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
        try {
            // 非同期処理の結果を同期的に取得
            String stringStatus = getInfo.getStatusJSON().get();
            JSONObject status = new JSONObject(stringStatus);
            String[] args = params.split("_");
            String serverName = args[0];
            for (String serverNameKey : status.keySet()) {
                if (serverNameKey.equals(serverName)) {
                    JSONObject serverStatus = status.getJSONObject(serverNameKey);
                    if (serverStatus.getBoolean("isOnline")) {
                        DynamicServerData serverData = new Gson().fromJson(serverStatus.getJSONObject("serverData").toString(), DynamicServerData.class);
                        return getStatus(serverData, args);
                    } else if (args.length == 1) {
                        return StatusReporter.getData().getPlaceholderMessage().getStatus_offline();
                    } else {
                        return "";
                    }
                }
            }
            return String.format("サーバー、'%s'は存在しません。",
                serverName
            );
        } catch (Exception e) {
            return "エラーが発生しました: " + e.getMessage();
        }
    }

    private String getStatus(DynamicServerData serverStatus, String[] args) {
        if(args.length == 1) {
            return switch(serverStatus.getStatus()) {
                case RUNNING -> StatusReporter.getData().getPlaceholderMessage().getStatus_online();
                case STARTING -> StatusReporter.getData().getPlaceholderMessage().getStarting();
                default -> "";
            };
        } else if(args.length == 2) {
            var placeholder = StatusReporter.getData().getPlaceholderMessage();

            String playerscount = String.format(
                    placeholder.getOnline_playerscount(),
                    serverStatus.getPlayers().size()
            );
            String version = String.format(
                    placeholder.getOnline_version(),
                    serverStatus.getVersion()
            );
            return switch(args[1]) {
                case "playerscount" ->  playerscount;
                case "version" -> version;
                default -> "";
            };
        }
        return "";
    }
}
