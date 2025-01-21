package org.luke.statusReporter.Data;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConfigData {
    PlaceholderMessage placeholderMessage;
    MessageStatus messageStatus;

    @Getter
    @Setter
    public static class MessageStatus {
        Boolean messageSwitch;
        MessageData messageData;
        Boolean filterSwitch;
        List<String> filter_servers;
    }

    @Getter
    @Setter
    public static class PlaceholderMessage {
        String status_online;
        String status_offline;
        String starting;
    }

    @Getter
    @Setter
    public static class MessageData {
        String starting;
        String started;
        String closed;
    }
}
