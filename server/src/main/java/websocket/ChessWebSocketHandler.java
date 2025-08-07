package websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.sql.MySqlDataAccess;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class ChessWebSocketHandler {
    private final DataAccess dataAccess = new MySqlDataAccess();
    private final Gson gson = new Gson();

    private static final Map<Session, Integer> gameSessions = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket Connected: " + session);
    }
    /*private void broadcastNotificationToOthers(int gameId, Session excludeSession, String notification) throws Exception {
        Set<Session> sessions = gameSessions.getOrDefault(gameId, Set.of());
        OutgoingMessage msg = new OutgoingMessage("notification", notification);
        String json = gson.toJson(msg);
        for (Session s : sessions) {
            if (!s.equals(excludeSession) && s.isOpen()) {
                s.getRemote().sendString(json);
            }
        }
    }/*


    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            WebSocketMessage msg = gson.fromJson(message, WebSocketMessage.class);

            if ("join".equals(msg.type)) {
                gameSessions.put(session, msg.gameId);
                GameData game = dataAccess.getGame(msg.gameId);
                session.getRemote().sendString(gson.toJson(game.game()));
            }

            if ("move".equals(msg.type)) {
                GameData gameData = dataAccess.getGame(msg.gameId);
                ChessGame game = gameData.game();
                game.makeMove(msg.move); // may want to validate legal moves


                GameData updated = new GameData(
                        gameData.gameID(),
                        gameData.whiteUsername(),
                        gameData.blackUsername(),
                        gameData.gameName(),
                        game
                );
                dataAccess.updateGame(updated);


                for (Map.Entry<Session, Integer> entry : gameSessions.entrySet()) {
                    if (entry.getValue().equals(msg.gameId)) {
                        entry.getKey().getRemote().sendString(gson.toJson(game));
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        gameSessions.remove(session);
        System.out.println("WebSocket Closed: " + reason);
    }
}
