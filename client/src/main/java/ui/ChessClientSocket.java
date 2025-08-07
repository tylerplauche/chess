package ui;

import com.google.gson.Gson;
import model.WebSocketMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.util.function.Consumer;

public class ChessClientSocket extends WebSocketAdapter {
    private final Consumer<WebSocketMessage> onMessage;
    private final Gson gson = new Gson();
    private boolean connected = false;

    public ChessClientSocket(Consumer<WebSocketMessage> onMessage) {
        this.onMessage = onMessage;
    }

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        connected = true;
        System.out.println("Client socket connected: " + session);
    }

    @Override
    public void onWebSocketText(String message) {
        try {
            WebSocketMessage msg = gson.fromJson(message, WebSocketMessage.class);
            onMessage.accept(msg);
            System.out.println("Received message: " + message);
        } catch (Exception e) {
            System.out.println("Failed to parse incoming message: " + message);
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        connected = false;
        System.out.println(" WebSocket closed: " + reason);
        super.onWebSocketClose(statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        System.out.println("WebSocket error: " + cause.getMessage());
        cause.printStackTrace();
    }

    public void send(String json) throws Exception {
        if (!connected || getRemote() == null) {
            throw new IllegalStateException("Cannot send: WebSocket not connected.");
        }
        getRemote().sendString(json);
    }

    public boolean isConnected() {
        return connected;
    }
}
