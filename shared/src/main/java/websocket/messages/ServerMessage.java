package websocket.messages;

import chess.ChessGame;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    private final ServerMessageType serverMessageType;

    // Optional content depending on type
    private String message;
    private String errorMessage;
    private String gameStateJson;
    private String payload;
    private ChessGame game;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }
    public void setGame(ChessGame game) {
        this.game = game;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }
    public void setPayload(String payload) {
        this.payload = payload;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setGameStateJson(String gameStateJson) {
        this.gameStateJson = gameStateJson;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getGameStateJson() {
        return gameStateJson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerMessage)) return false;
        ServerMessage that = (ServerMessage) o;
        return serverMessageType == that.serverMessageType &&
                Objects.equals(message, that.message) &&
                Objects.equals(errorMessage, that.errorMessage) &&
                Objects.equals(gameStateJson, that.gameStateJson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverMessageType, message, errorMessage, gameStateJson);
    }
}
