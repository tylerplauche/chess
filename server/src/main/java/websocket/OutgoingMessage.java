package websocket;

public class OutgoingMessage {
    public String type;
    public Object data;

    public OutgoingMessage(String type, Object data) {
        this.type = type;
        this.data = data;
    }
}
