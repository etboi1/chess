package websocket.messages;

import com.google.gson.Gson;

public class NotificationMessage extends ServerMessage {

    public final String message;

    public NotificationMessage(ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }

    public String getMessage() { return message; }

    public String toString() {return new Gson().toJson(message); }
}
