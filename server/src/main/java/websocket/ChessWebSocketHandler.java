package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.sql.AuthTokenDAOSQL;
import dataaccess.sql.GameDAOSQL;
import dataaccess.sql.MySqlDataAccess;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import websocket.commands.*;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


@WebSocket
public class ChessWebSocketHandler {

    private static final DataAccess DATAACCESS = new MySqlDataAccess();
    private static final Gson GSON = new Gson();
    private final GameDAO gameDAO = new GameDAOSQL();
    private static final Map<Session, Integer> GAMESESSIONS = new ConcurrentHashMap<>();
    private static final Map<Integer, Session> WHITE_PLAYER_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<Integer, Session> BLACK_PLAYER_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<Integer, Map<Session, String>> OBSERVERS_BY_GAME_ID = new ConcurrentHashMap<>();
    //private static final LeaveGameService leaveGameService = new LeaveGameService(DATAACCESS);



    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket Connected: " + session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            UserGameCommand command = GSON.fromJson(message, UserGameCommand.class);
            int gameID = command.getGameID();


            AuthData auth = DATAACCESS.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(session, "Invalid or expired auth token");
                return;
            }


            GAMESESSIONS.put(session, gameID);

            switch (command.getCommandType()) {
                case CONNECT:
                    GameData gameData = DATAACCESS.getGame(gameID);
                    if (gameData == null) {
                        sendError(session, "Invalid game ID: " + gameID);
                        return;
                    }

                    AuthTokenDAOSQL authToken = new AuthTokenDAOSQL();
                    AuthData authData = authToken.getToken(command.getAuthToken());
                    String joinMessage = authData.username() + " has joined as ";



                    if (authData.username().equals(gameData.whiteUsername())){
                        joinMessage += "white";
                    }
                    else if (authData.username().equals(gameData.blackUsername())){
                        joinMessage += "black";
                    }
                    else{
                        joinMessage += "observer";
                    }


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
                case LEAVE:
                    handleLeave(session, command);
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
            AuthData auth = DATAACCESS.getAuth(command.getAuthToken());
            GameData gameData = DATAACCESS.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Game not found with ID " + command.getGameID());
                return;
            }

            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGame.setGame(gameData.game());
            session.getRemote().sendString(GSON.toJson(loadGame));


            if (null==auth.username()){
                return;
            }
            if  ("white".equals(auth.username())) {

                WHITE_PLAYER_SESSIONS.put(command.getGameID(), session);
            } else if ("black".equals(auth.username())) {
                BLACK_PLAYER_SESSIONS.put(command.getGameID(), session);
            } else if ("observer".equals(auth.username())) {
                // Observer
                OBSERVERS_BY_GAME_ID.computeIfAbsent(command.getGameID(), k -> new ConcurrentHashMap<>())
                        .put(session, command.getUsername());
            }




        } catch (Exception e) {
            //sendError(session, "Failed to load game: " + e.getMessage());
        }
    }


    private void handleMove(Session session, UserGameCommand command) {
        String authToken = command.getAuthToken();
        Integer gameId = command.getGameID();
        ChessMove move = command.getMove();

        try {
            AuthData auth = DATAACCESS.getAuth(authToken);
            if (auth == null) {
                sendError(session, "Invalid auth token.");
                return;
            }

            GameData gameData = DATAACCESS.getGame(gameId);
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



            ChessGame.TeamColor playerColor;
            if (gameData.whiteUsername().equals(auth.username())) {
                playerColor = ChessGame.TeamColor.WHITE;
            } else if (gameData.blackUsername().equals(auth.username())) {
                playerColor = ChessGame.TeamColor.BLACK;
            } else {
                sendError(session, "You are not a player in this game.");
                return;
            }

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



            ((MySqlDataAccess) DATAACCESS).updateGameState(gameId, GSON.toJson(game));


            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
            loadGame.setGame(game);
            broadcastToGame(gameId, loadGame);


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
            GameData gameData = DATAACCESS.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Game not found.");
                return;
            }

            // Retrieve user from auth token
            AuthData auth = DATAACCESS.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(session, "Invalid or expired auth token.");
                return;
            }

            String username = auth.username();

            // Check if the user is a player (white or black)
            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                sendError(session, "Only players can resign.");
                return;
            }

            ChessGame game = gameData.game();

            if (game.isGameOver()) {
                sendError(session, "Game is already over; no resign allowed.");
                return;
            }

            game.setGameOver(true);  // Mark game as over on resignation

            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(username + " has resigned");
            broadcastToGame(command.getGameID(), notification);

            ((MySqlDataAccess) DATAACCESS).updateGameState(command.getGameID(), GSON.toJson(game));

        } catch (Exception e) {
            sendError(session, "Failed to process resignation: " + e.getMessage());
        }
    }

    private void handleLeave(Session session, UserGameCommand command) {
        try {
            GameData gameData = DATAACCESS.getGame(command.getGameID());
            if (gameData == null) {
                sendError(session, "Game not found.");
                return;
            }

            AuthData auth = DATAACCESS.getAuth(command.getAuthToken());
            if (auth == null) {
                sendError(session, "Invalid or expired auth token.");
                return;
            }

            String username = auth.username();
            int gameId = command.getGameID();

            boolean isWhite = username.equals(gameData.whiteUsername());
            boolean isBlack = username.equals(gameData.blackUsername());

            Session whiteSession = WHITE_PLAYER_SESSIONS.get(gameId);
            Session blackSession = BLACK_PLAYER_SESSIONS.get(gameId);

            String leaveMessage = username + " has left the game.";
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.setMessage(leaveMessage);

            if (isWhite) {
                // Remove from white player sessions map
                WHITE_PLAYER_SESSIONS.remove(gameId);

                // Remove from gameSessions map as well
                GAMESESSIONS.remove(session);

                // Broadcast to others that white left
                broadcastToOthers(gameId, session, notification);

            } else if (isBlack) {
                BLACK_PLAYER_SESSIONS.remove(gameId);
                GAMESESSIONS.remove(session);
                broadcastToOthers(gameId, session, notification);
            } else {
                // Assume observer
                Map<Session, String> observers = OBSERVERS_BY_GAME_ID.get(gameId);
                if (observers != null) {
                    observers.remove(session);
                }

                // Remove observer session from gameSessions
                GAMESESSIONS.remove(session);

                // Notify remaining player only if one is left
                int activePlayers = 0;
                if (whiteSession != null && whiteSession.isOpen()) {
                    activePlayers++;
                }
                if (blackSession != null && blackSession.isOpen()) {
                    activePlayers++;
                }

                if (activePlayers == 1) {
                    Session remainingPlayer = (whiteSession != null && whiteSession.isOpen()) ? whiteSession : blackSession;
                    if (remainingPlayer != null && remainingPlayer.isOpen()) {
                        remainingPlayer.getRemote().sendString(GSON.toJson(notification));
                    }
                }
            }

            // Update game data to remove leaving player from DB/game state
            String white = gameData.whiteUsername();
            String black = gameData.blackUsername();

            if (Objects.equals(white, username)) {
                GameData updatedGame = new GameData(
                        gameData.gameID(),
                        null, // whiteUsername cleared
                        black,
                        gameData.gameName(),
                        gameData.game()
                );
                gameDAO.updateGame(updatedGame.gameID(), "WHITE", updatedGame.whiteUsername());
            } else if (Objects.equals(black, username)) {
                GameData updatedGame = new GameData(
                        gameData.gameID(),
                        white,
                        null, // blackUsername cleared
                        gameData.gameName(),
                        gameData.game()
                );
                DATAACCESS.updateGame(updatedGame);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(session, "Failed to process leave: " + e.getMessage());
        }
    }


    private void sendError(Session session, String errorMsg) {
        try {
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
            error.setErrorMessage(errorMsg);
            session.getRemote().sendString(GSON.toJson(error));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastToGame(int gameID, ServerMessage message) {
        String json = GSON.toJson(message);
        for (Map.Entry<Session, Integer> entry : GAMESESSIONS.entrySet()) {
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
        String json = GSON.toJson(message);
        for (Map.Entry<Session, Integer> entry : GAMESESSIONS.entrySet()) {
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
        Integer gameID = GAMESESSIONS.remove(session);
        if (gameID != null) {
            WHITE_PLAYER_SESSIONS.remove(gameID, session);
            BLACK_PLAYER_SESSIONS.remove(gameID, session);

            Map<Session, String> observers = OBSERVERS_BY_GAME_ID.get(gameID);
            if (observers != null) {
                observers.remove(session);
                if (observers.isEmpty()) {
                    OBSERVERS_BY_GAME_ID.remove(gameID);
                }
            }
        }

        System.out.println("WebSocket Closed (" + statusCode + "): " + reason);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.out.println("WebSocket Error: " + error.getMessage());
        error.printStackTrace();
    }
}
