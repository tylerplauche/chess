package websocket.commands;

import chess.ChessMove;

public class MakeMove extends UserGameCommand {
    private final String username;

    public MakeMove(String authToken, Integer gameID, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        setMove(move);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
