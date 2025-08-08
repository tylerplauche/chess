package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
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
        String authToken = command.getAuthToken();
        Integer gameId = command.getGameID();
        ChessMove move = command.getMove();

        try {
            AuthData auth = dataAccess.getAuth(authToken);
            if (auth == null) {
                sendError(session, "Invalid auth token.");
                return;
            }

            GameData gameData = dataAccess.getGame(gameId);
            if (gameData == null) {
                sendError(session, "Game not found.");
                return;
            }

            ChessGame game = gameData.game();


            if (game.isGameOver()) {
                sendError(session, "Game is already over; no moves allowed.");
                return;
            }
            System.out.println("Game over status: " + game.isGameOver());


            // Determine player color based on username
            ChessGame.TeamColor playerColor;
            if (gameData.whiteUsername().equals(auth.username())) {
                playerColor = ChessGame.TeamColor.WHITE;
            } else if (gameData.blackUsername().equals(auth.username())) {
                playerColor = ChessGame.TeamColor.BLACK;
            } else {
                sendError(session, "You are not a player in this game.");
                return;
            }

            // Check if it's the player's turn
            if (game.getTeamTurn() != playerColor) {
                sendError(session, "It's not your turn.");
                return;
            }

            // Check the piece at the start position exists and belongs to the player
            var piece = game.getBoard().getPiece(move.getStartPosition());
            if (piece == null) {
                sendError(session, "No piece at the source position.");
                return;
            }
            if (piece.getTeamColor() != playerColor) {
                sendError(session, "You cannot move the opponent's piece.");
                return;
            }

            try {
                game.makeMove(move);
            } catch (InvalidMoveException ime) {
                sendError(session, "Invalid move: " + ime.getMessage());
                return;
            }


            // Persist updated game state
            ((MySqlDataAccess) dataAccess).updateGameState(gameId, gson.toJson(game));

            // Send updated game state to all players
            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGame.setGame(game);
            broadcastToGame(gameId, loadGame);

            // Broadcast notification about the move to other players (exclude mover)
            String mover = auth.username();
            String moveText = mover + " moved from " + move.getStartPosition() + " to " + move.getEndPosition();
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(moveText);
            broadcastToOthers(gameId, session, notification);

        } catch (DataAccessException e) {
            sendError(session, "Internal error: " + e.getMessage());
        } catch (Exception e) {
            sendError(session, "Move failed: " + e.getMessage());
        }

    }



    private void handleResign(Session session, UserGameCommand command) {
        try {
            GameData gameData = dataAccess.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Game not found.");
                return;
            }
            ChessGame game = gameData.game();
            game.setGameOver(true);  // Mark game as over on resignation

            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(command.getUsername() + " has resigned");
            broadcastToGame(command.getGameID(), notification);

            ((MySqlDataAccess) dataAccess).updateGameState(command.getGameID(), gson.toJson(game));

        } catch (Exception e) {
            sendError(session, "Failed to process resignation: " + e.getMessage());
        }
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
