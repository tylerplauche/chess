package websocket.commands;

public class Resign extends UserGameCommand {
    private final String username;

    public Resign(String authToken, Integer gameID) {
        super(CommandType.RESIGN, authToken, gameID);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
