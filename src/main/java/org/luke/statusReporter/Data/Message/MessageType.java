package org.luke.statusReporter.Data.Message;

public class MessageType {
    public enum MessageServer {
        REGISTER_RESULT,
        SEND_INFO,
        UPDATE_REGISTERED,
        UPDATE_STARTED,
        UPDATE_CLOSED,
        UPDATE_INFO, // ほかのサーバーがSEND_INFO(情報が更新されたら)送信
    }
    public enum MessageClient {
        REGISTER,
        STARTED,
        SEND_INFO,
    }
}
