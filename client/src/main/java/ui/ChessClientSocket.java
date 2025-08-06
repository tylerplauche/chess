package websocket;

import model.WebSocketMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import com.google.gson.Gson;

import java.util.function.Consumer;

public class ChessClientSocket extends WebSocketAdapter {
    private final Consumer<WebSocketMessage> onMessage;
    private final Gson gson = new Gson();

    public ChessClientSocket(Consumer<WebSocketMessage> onMessage) {
        this.onMessage = onMessage;
    }

    @Override
    public void onWebSocketText(String message) {
        WebSocketMessage msg = gson.fromJson(message, WebSocketMessage.class);
        onMessage.accept(msg);
    }

    public void send(String json) throws Exception {
        getRemote().sendString(json);
    }
}
