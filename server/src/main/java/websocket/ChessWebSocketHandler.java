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
        try {
            WebSocketMessage msg = gson.fromJson(message, WebSocketMessage.class);
            Session session = getSession();

            if ("join".equals(msg.type)) {
                sessions.put(session, msg.gameId);
                GameData gameData = db.getGame(msg.gameId);
                session.getRemote().sendString(gson.toJson(gameData.game()));
            }

            if ("move".equals(msg.type)) {
                GameData gameData = db.getGame(msg.gameId);
                ChessGame game = gameData.game();
                game.makeMove(msg.move);


                GameData updated = new GameData(
                        gameData.gameID(),
                        gameData.whiteUsername(),
                        gameData.blackUsername(),
                        gameData.gameName(),
                        game
                );
                db.updateGame(updated);

                // Broadcast to all in same game
                for (Map.Entry<Session, Integer> entry : sessions.entrySet()) {
                    if (entry.getValue().equals(msg.gameId)) {
                        entry.getKey().getRemote().sendString(gson.toJson(game));
                    }
                }
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
}
