import chess.*;
import server.Server;
import dataaccess.DatabaseManager;
import dataaccess.DataAccessException;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        try {
            DatabaseManager.createDatabase();
            DatabaseManager.createTables();
        } catch (DataAccessException ex) {
            System.err.println("Failed to initialize database: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        Server server = new Server();
        int port = 8080;
        server.run(port);

    }
}