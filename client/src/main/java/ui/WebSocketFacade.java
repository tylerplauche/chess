package ui;

import chess.ChessMove;
import com.google.gson.Gson;
import model.OutgoingMessage;
import model.WebSocketMessage;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.function.Consumer;

public class WebSocketFacade {
    private final WebSocketClient client = new WebSocketClient();
    private ChessClientSocket socket;
    private final Gson gson = new Gson();
    private Consumer<WebSocketMessage> onMessageHandler;

    public void connect(String uri, Consumer<WebSocketMessage> onMessage) throws Exception {
        this.onMessageHandler = onMessage;  // Save the UI's message handler
        client.start();
        socket = new ChessClientSocket(onMessageHandler);  // Pass handler directly
        client.connect(socket, new URI(uri)).get();
        System.out.println("Connected to server at " + uri);
    }

    private void send(OutgoingMessage message) throws Exception {
        if (socket == null || !socket.isConnected()) {
            throw new IllegalStateException("WebSocket not connected");
        }
        String json = gson.toJson(message);
        socket.send(json);
    }

    public void close() throws Exception {
        if (client != null) {
            client.stop();
            System.out.println("WebSocket closed");
        }
    }

    public void sendJoin(int gameId, String color) {
        try {
            send(OutgoingMessage.join(gameId, color));
        } catch (Exception e) {
            System.out.println("Failed to send join: " + e.getMessage());
        }
    }

    public void sendMove(int gameId, ChessMove move) {
        try {
            send(OutgoingMessage.move(gameId, move));
        } catch (Exception e) {
            System.out.println("Failed to send move: " + e.getMessage());
        }
    }

    public void sendResign(int gameId) {
        try {
            send(OutgoingMessage.resign(gameId));
        } catch (Exception e) {
            System.out.println("Failed to send resign: " + e.getMessage());
        }
    }
}
