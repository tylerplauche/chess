package model;

import chess.ChessGame;
import chess.ChessMove;

public class WebSocketMessage {
    public ChessMove move;
    public int gameId;
    public String type;
    public String serverMessageType;
    public ChessGame game;
    public String errorMessage;
    public String message;






    public String getServerMessageType() { return serverMessageType; }
    public ChessGame getGame() { return game; }
    public String getErrorMessage() { return errorMessage; }
    public String getMessage() { return message; }
}
