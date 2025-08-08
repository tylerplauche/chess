package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.sql.MySqlDataAccess;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.messages.ErrorMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import websocket.commands.*;

import java.io.IOException;
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
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            int gameID = command.getGameID();


            AuthData auth = dataAccess.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(session, "Invalid or expired auth token");
                return;
            }


            gameSessions.put(session, gameID);

            switch (command.getCommandType()) {
                case CONNECT:
                    GameData gameData = dataAccess.getGame(gameID);
                    if (gameData == null) {
                        sendError(session, "Invalid game ID: " + gameID);
                        return;
                    }

                    String joiner = command.getUsername();
                    String joinMessage = joiner + " has joined as " + command.getPlayerColor();

                    Notification notification = new Notification(joinMessage);
                    broadcastToOthers(gameID, session, notification);

                    handleJoin(session, command);
                    break;

                case MAKE_MOVE:
                    handleMove(session, command);
                    break;

                case RESIGN:
                    handleResign(session, command);
                    break;

                default:
                    sendError(session, "Unknown command type: " + command.getCommandType());
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(session, "Invalid command: " + e.getMessage());
        }
    }

    private void handleJoin(Session session, UserGameCommand command) {
        try {
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Game not found with ID " + command.getGameID());
                return;
            }

            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGame.setGame(gameData.game()); // Set actual ChessGame object

            session.getRemote().sendString(gson.toJson(loadGame));
        } catch (Exception e) {
            sendError(session, "Failed to load game: " + e.getMessage());
        }
    }

    private void handleMove(Session session, UserGameCommand command) {
        try {
            ((MySqlDataAccess) dataAccess).updateGameState(command.getGameID(), gson.toJson(command.getGame()));

            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGame.setGame(command.getGame());

            broadcastToGame(command.getGameID(), loadGame);
        } catch (DataAccessException e) {
            sendError(session, "Failed to update game: " + e.getMessage());
        }
    }

    private void handleResign(Session session, UserGameCommand command) {
        ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        notification.setPayload(command.getUsername() + " has resigned");

        broadcastToGame(command.getGameID(), notification);
    }

    private void sendError(Session session, String errorMsg) {
        try {
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            error.setErrorMessage(errorMsg);
            session.getRemote().sendString(gson.toJson(error));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastToGame(int gameID, ServerMessage message) {
        String json = gson.toJson(message);
        for (Map.Entry<Session, Integer> entry : gameSessions.entrySet()) {
            if (entry.getValue() == gameID && entry.getKey().isOpen()) {
                try {
                    entry.getKey().getRemote().sendString(json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void broadcastToOthers(int gameID, Session excludeSession, ServerMessage message) {
        String json = gson.toJson(message);
        for (Map.Entry<Session, Integer> entry : gameSessions.entrySet()) {
            if (entry.getValue() == gameID && entry.getKey().isOpen() && !entry.getKey().equals(excludeSession)) {
                try {
                    entry.getKey().getRemote().sendString(json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
