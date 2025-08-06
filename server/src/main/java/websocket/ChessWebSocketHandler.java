package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.sql.MySqlDataAccess;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChessWebSocketHandler extends WebSocketAdapter {
    private static final Gson gson = new Gson();
    private static final DataAccess db = new MySqlDataAccess();

    private static final Map<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();
    private static final Map<Session, Integer> sessionToGame = new ConcurrentHashMap<>();

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
                case "join" -> processJoin(session, msg);
                case "move" -> processMove(session, msg);
                default -> sendError(session, "Unknown message type: " + msg.type);
            }

        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processJoin(Session session, WebSocketMessage msg) throws Exception {
        if (msg.gameId == null) {
            sendError(session, "Missing gameId.");
            return;
        }

        GameData gameData = db.getGame(msg.gameId);
        if (gameData == null) {
            sendError(session, "Game not found.");
            return;
        }

        gameSessions.computeIfAbsent(msg.gameId, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionToGame.put(session, msg.gameId);

        sendGameState(session, gameData.game());
    }

    private void processMove(Session session, WebSocketMessage msg) throws Exception {
        if (msg.gameId == null || msg.move == null) {
            sendError(session, "Missing gameId or move.");
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

        db.updateGame(new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        ));

        broadcastToGame(msg.gameId, new OutgoingMessage("gameState", game));
    }

    private void sendGameState(Session session, ChessGame game) throws Exception {
        String json = gson.toJson(new OutgoingMessage("gameState", game));
        session.getRemote().sendString(json);
    }

    private void broadcastToGame(int gameId, OutgoingMessage message) throws Exception {
        String json = gson.toJson(message);
        Set<Session> sessions = gameSessions.getOrDefault(gameId, Set.of());

        for (Session s : sessions) {
            if (s.isOpen()) {
                s.getRemote().sendString(json);
            }
        }
    }

    private void sendError(Session session, String error) {
        try {
            if (session != null && session.isOpen()) {
                session.getRemote().sendString(gson.toJson(new OutgoingMessage("error", error)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        Session session = getSession();
        Integer gameId = sessionToGame.remove(session);

        if (gameId != null) {
            Set<Session> sessions = gameSessions.get(gameId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    gameSessions.remove(gameId);
                }
            }
        }

        System.out.println("WebSocket closed: " + reason);
        super.onWebSocketClose(statusCode, reason);
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace();
    }
}
