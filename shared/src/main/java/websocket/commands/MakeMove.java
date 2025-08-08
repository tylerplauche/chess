package websocket.commands;

import chess.ChessMove;

public class MakeMove extends UserGameCommand {
    private final String username;

    public MakeMove(String username, Integer gameID, ChessMove move) {
        // Assuming the username is used as authToken in super
        super(CommandType.MAKE_MOVE, username, gameID);
        setMove(move);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
