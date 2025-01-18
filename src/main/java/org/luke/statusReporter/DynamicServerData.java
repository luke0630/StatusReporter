package org.luke.statusReporter;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DynamicServerData {
    private String serverName = "";
    private StatusReporter.ServerStatus status;
    private List<PluginInfo> plugins;
    private List<PlayerData> players;

    @Setter
    @Getter
    public static class PluginInfo {
        private String pluginName = "";
        private String mainClass;
        private String description;
        private String version;
        private List<String> authors;
        private String website;
    }

    @Setter
    @Getter
    public static class PlayerData {
        private String uuid;
        private String name;
    }
}
