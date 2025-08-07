package websocket.commands;

import chess.ChessGame.TeamColor;

public class JoinPlayer extends UserGameCommand {
    private final String username;

    public JoinPlayer(String authToken, Integer gameID, TeamColor playerColor) {
        super(CommandType.CONNECT, authToken, gameID);
        setPlayerColor(playerColor);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}