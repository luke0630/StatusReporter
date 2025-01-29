package org.luke.statusReporter.Utility;

import lombok.experimental.UtilityClass;
import org.json.JSONObject;
import org.luke.statusReporter.Data.Message.MessageType;

@UtilityClass
public class MessageUtility {
    public String getResultResponse(MessageType.MessageClient type, JSONObject content) {
        return getResponse(type, content);
    }
    public String getResultResponse(MessageType.MessageServer type, JSONObject content) {
        return getResponse(type, content);
    }

    String getResponse(Enum<?> type, JSONObject content) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("content", content.toString());

        return jsonObject.toString();
    }
}
