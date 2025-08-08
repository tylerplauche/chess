package websocket.commands;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessGame.TeamColor;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    private final CommandType commandType;
    private String username;
    private final String authToken;
    private final Integer gameID;

    private ChessGame game;
    private ChessMove move;
    private TeamColor playerColor;

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    // Optional convenience constructor
    public UserGameCommand(CommandType commandType, String authToken, Integer gameID, TeamColor playerColor) {
        this(commandType, authToken, gameID);
        this.playerColor = playerColor;
    }

    public CommandType getCommandType() {
        return commandType;
    }
    public String getPlayerColorAsString() {
        return playerColor == null ? null : playerColor.name();
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    public ChessGame getGame() {
        return game;
    }

    public void setGame(ChessGame game) {
        this.game = game;
    }

    public ChessMove getMove() {
        return move;
    }

    public void setMove(ChessMove move) {
        this.move = move;
    }

    public TeamColor getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(TeamColor playerColor) {
        this.playerColor = playerColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGameCommand)) return false;
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID()) &&
                Objects.equals(getMove(), that.getMove()) &&
                getPlayerColor() == that.getPlayerColor();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID(), getMove(), getPlayerColor());
    }
}
