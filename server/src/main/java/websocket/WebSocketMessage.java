package websocket;

import chess.ChessMove;

public class WebSocketMessage {
    public String type;      // "join" or "move"
    public Integer gameId;
    public ChessMove move;   // Only used if type is "move"
    public String authToken; // Optional: can be used to validate
}
