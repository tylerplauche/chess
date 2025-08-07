package model;

import chess.ChessGame;
import chess.ChessMove;

public class WebSocketMessage {
    public String type;
    public String serverMessageType;
    public int gameId;
    public ChessMove move;
    public ChessGame game;
    public String errorMessage;
    public String message;
}
