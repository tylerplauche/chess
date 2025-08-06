package model;

import chess.ChessGame;
import chess.ChessMove;

public class WebSocketMessage {
    private String serverMessageType;
    private ChessGame game;
    private String errorMessage;
    private String message;

    // Getters
    public String getServerMessageType() { return serverMessageType; }
    public ChessGame getGame() { return game; }
    public String getErrorMessage() { return errorMessage; }
    public String getMessage() { return message; }
}
