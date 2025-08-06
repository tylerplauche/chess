package websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import model.*;

import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class ChessWebSocketHandler {
    private static final ConcurrentHashMap<Integer, Session> sessions = new ConcurrentHashMap<>();
    private final DataAccess dataAccess;
    private final Gson gson = new Gson();

    public ChessWebSocketHandler(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected: " + session);
        // You’ll probably want to track which game and user this is
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            // Deserialize message (e.g., move or join)
            WebSocketMessage msg = gson.fromJson(message, WebSocketMessage.class);

            if (msg.type.equals("move")) {
                GameData gameData = dataAccess.getGame(msg.gameId);
                ChessGame game = gameData.getGame();

                game.makeMove(msg.move); // Handle exceptions
                gameData = new GameData(
                        gameData.gameID(),
                        gameData.whiteUsername(),
                        gameData.blackUsername(),
                        gameData.gameName(),
                        game
                );
                dataAccess.updateGame(gameData);

                String gameJson = gson.toJson(game);
                broadcastToAll(msg.gameId, gameJson);
            }

            if (msg.type.equals("join")) {
                sessions.put(msg.gameId, session);
                GameData gameData = dataAccess.getGame(msg.gameId);
                String gameJson = gson.toJson(gameData.getGame());
                session.getRemote().sendString(gameJson);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Disconnected: " + session);
        sessions.values().remove(session);
    }

    private void broadcastToAll(int gameId, String message) {
        sessions.forEach((id, session) -> {
            if (id == gameId && session.isOpen()) {
                try {
                    session.getRemote().sendString(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
