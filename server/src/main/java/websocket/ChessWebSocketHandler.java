package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.sql.MySqlDataAccess;
import model.GameData;
import model.WebSocketMessage;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class ChessWebSocketHandler {
    private static final DataAccess dataAccess = new MySqlDataAccess();
    private static final Gson gson = new Gson();
    private static final Map<Session, Integer> gameSessions = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket Connected: " + session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            WebSocketMessage msg = gson.fromJson(message, WebSocketMessage.class);

            switch (msg.type) {
                case "join" -> {
                    gameSessions.put(session, msg.gameId);
                    GameData gameData = dataAccess.getGame(msg.gameId);

                    if (session.isOpen() && gameData != null) {
                        WebSocketMessage loadMsg = new WebSocketMessage();
                        loadMsg.type = "gameUpdate";
                        loadMsg.game = gameData.game();

                        session.getRemote().sendString(gson.toJson(loadMsg), null);
                        System.out.println("Player joined game: " + msg.gameId);
                    }
                }


                case "move" -> {
                    GameData gameData = dataAccess.getGame(msg.gameId);
                    if (gameData == null) return;

                    ChessGame game = gameData.game();
                    game.makeMove(msg.move);

                    GameData updated = new GameData(
                            gameData.gameID(),
                            gameData.whiteUsername(),
                            gameData.blackUsername(),
                            gameData.gameName(),
                            game
                    );
                    dataAccess.updateGame(updated);

                    WebSocketMessage updateMsg = new WebSocketMessage();
                    updateMsg.type = "gameUpdate";  // use 'type', and use the type your client expects
                    updateMsg.game = game;

                    String gameJson = gson.toJson(updateMsg);

                    for (Map.Entry<Session, Integer> entry : gameSessions.entrySet()) {
                        Session s = entry.getKey();
                        if (entry.getValue().equals(msg.gameId) && s.isOpen()) {
                            s.getRemote().sendString(gameJson, null);
                        }
                    }
                }


                default -> System.out.println("Unrecognized message type: " + msg.type);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        gameSessions.remove(session);
        System.out.println("WebSocket Closed (" + statusCode + "): " + reason);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket Error: " + error.getMessage());
        error.printStackTrace();
    }
}
