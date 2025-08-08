package websocket.messages;

public class Notification extends ServerMessage {
    public Notification(String message) {
        super(ServerMessageType.NOTIFICATION);
        setMessage(message);  // set the 'message' field, not payload
    }
}


    // Optional getter for convenience


