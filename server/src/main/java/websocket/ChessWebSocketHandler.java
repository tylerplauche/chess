package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.sql.MySqlDataAccess;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChessWebSocketHandler extends WebSocketAdapter {
    private static final Map<Session, Integer> sessions = new ConcurrentHashMap<>();
    private static final DataAccess db = new MySqlDataAccess();
    private static final Gson gson = new Gson();

    @Override
    public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        System.out.println("WebSocket connected: " + session);
    }

    @Override
    public void onWebSocketText(String message) {
        Session session = getSession();

        try {
            WebSocketMessage msg = gson.fromJson(message, WebSocketMessage.class);

            if (msg == null || msg.type == null) {
                sendError(session, "Invalid message format.");
                return;
            }

            switch (msg.type) {
                case "join" -> handleJoin(session, msg);
                case "move" -> handleMove(session, msg);
                default -> sendError(session, "Unknown message type: " + msg.type);
            }

        } catch (Exception e) {
            sendError(session, "Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleJoin(Session session, WebSocketMessage msg) throws Exception {
        if (msg.gameId == null) {
            sendError(session, "Missing gameId in join request.");
            return;
        }

        sessions.put(session, msg.gameId);
        GameData gameData = db.getGame(msg.gameId);

        if (gameData == null) {
            sendError(session, "Game not found.");
            return;
        }

        String response = gson.toJson(new OutgoingMessage("gameState", gameData.game()));
        session.getRemote().sendString(response);
    }

    private void handleMove(Session session, WebSocketMessage msg) throws Exception {
        if (msg.gameId == null || msg.move == null) {
            sendError(session, "Missing gameId or move in move request.");
            return;
        }

        GameData gameData = db.getGame(msg.gameId);
        if (gameData == null) {
            sendError(session, "Game not found.");
            return;
        }

        ChessGame game = gameData.game();

        try {
            game.makeMove(msg.move);
        } catch (Exception e) {
            sendError(session, "Invalid move: " + e.getMessage());
            return;
        }

        GameData updated = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );
        db.updateGame(updated);

        broadcastToGame(msg.gameId, new OutgoingMessage("gameState", game));
    }

    private void broadcastToGame(int gameId, OutgoingMessage message) throws Exception {
        String json = gson.toJson(message);
        for (Map.Entry<Session, Integer> entry : sessions.entrySet()) {
            if (entry.getValue().equals(gameId)) {
                Session s = entry.getKey();
                if (s.isOpen()) {
                    s.getRemote().sendString(json);
                }
            }
        }
    }

    private void sendError(Session session, String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getRemote().sendString(gson.toJson(new OutgoingMessage("error", message)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        sessions.remove(getSession());
        System.out.println("WebSocket closed: " + reason);
        super.onWebSocketClose(statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace();
    }
}
