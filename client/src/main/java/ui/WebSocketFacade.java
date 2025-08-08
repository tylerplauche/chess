package ui;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class WebSocketFacade implements WebSocket.Listener {
    private WebSocket webSocket;
    private final Gson gson = new Gson();
    private Consumer<ServerMessage> messageHandler;

    /**
     * Connect to the WebSocket server at the given URL, with a message handler.
     */
    public void connect(String url, Consumer<ServerMessage> messageHandler) {
        this.messageHandler = messageHandler;

        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(url), this)
                .thenAccept(ws -> this.webSocket = ws)
                .join();
    }

    /**
     * Send a UserGameCommand as JSON text.
     */
    public void send(UserGameCommand command) {
        if (webSocket == null) {
            throw new IllegalStateException("WebSocket is not connected");
        }
        String json = gson.toJson(command);
        webSocket.sendText(json, true);
    }

    /**
     * Close the WebSocket connection gracefully.
     */


    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("WebSocket connection opened");
        Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            // Parse incoming JSON string into ServerMessage object
            ServerMessage message = gson.fromJson(data.toString(), ServerMessage.class);
            if (messageHandler != null) {
                messageHandler.accept(message);
            } else {
                System.out.println("No message handler set for WebSocketFacade");
            }
        } catch (Exception e) {
            System.out.println("Failed to parse incoming message: " + data);
            e.printStackTrace();
        }
        return Listener.super.onText(webSocket, data, last);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.out.println("WebSocket error: " + error.getMessage());
        error.printStackTrace();
    }
}
