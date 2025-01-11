package org.luke.statusReporter.JSON;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.manager.DynamicServerData;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class PluginsInfo {
    public static List<DynamicServerData.PluginInfo> get() {
        Plugin[] plugins = getServer().getPluginManager().getPlugins();

        List<DynamicServerData.PluginInfo> pluginInfoList = new ArrayList<>();

        for (Plugin plugin : plugins) {
            PluginDescriptionFile description = plugin.getDescription();

            DynamicServerData.PluginInfo pluginInfo = new DynamicServerData.PluginInfo();
            pluginInfo.setPluginName(description.getName());
            pluginInfo.setVersion(description.getVersion());
            pluginInfo.setMainClass(description.getMain());
            pluginInfo.setAuthors(description.getAuthors());
            pluginInfo.setDescription(description.getDescription());
            pluginInfo.setWebsite(description.getWebsite());

            pluginInfoList.add(pluginInfo);
        }

        return pluginInfoList;
    }
}
