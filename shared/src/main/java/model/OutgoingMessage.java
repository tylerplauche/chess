// model/OutgoingMessage.java
package model;

import chess.ChessMove;

public class OutgoingMessage {
    public String type; // "join", "move", "resign"
    public Integer gameId;
    public String playerColor; // for "join"
    public ChessMove move;     // for "move"

    public OutgoingMessage(String type, Integer gameId, String playerColor, ChessMove move) {
        this.type = type;
        this.gameId = gameId;
        this.playerColor = playerColor;
        this.move = move;
    }

    public static OutgoingMessage join(int gameId, String color) {
        return new OutgoingMessage("join", gameId, color, null);
    }

    public static OutgoingMessage move(int gameId, ChessMove move) {
        return new OutgoingMessage("move", gameId, null, move);
    }

    public static OutgoingMessage resign(int gameId) {
        return new OutgoingMessage("resign", gameId, null, null);
    }
}
