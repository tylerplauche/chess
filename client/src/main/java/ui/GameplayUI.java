package ui;

import model.GameData;
import ui.WebSocketFacade;
import model.WebSocketMessage;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Scanner;

public class GameplayUI {
    private final Scanner scanner = new Scanner(System.in);
    private final WebSocketFacade ws;
    private final int gameId;
    private final String username;
    private final String playerColor;

    public GameplayUI(WebSocketFacade ws, int gameId, String username, String playerColor) {
        this.ws = ws;
        this.gameId = gameId;
        this.username = username;
        this.playerColor = playerColor; // "WHITE", "BLACK", or "OBSERVER"
    }

    public void run() {
        System.out.println("\nJoined game " + gameId + " as " + playerColor + ".");
        ws.sendJoin(gameId, playerColor);

        while (true) {
            System.out.println("\nChoose a command:");
            System.out.println("1. Make Move");
            System.out.println("2. Resign");
            System.out.println("3. Refresh Board");
            System.out.println("4. Leave Game");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> makeMove();
                case "2" -> {
                    System.out.println("Resigning...");
                    ws.sendResign(gameId);
                    return;
                }
                case "3" -> {
                    System.out.println("Waiting for server to update board...");
                    // Server will send game state via WebSocket automatically
                }
                case "4" -> {
                    System.out.println("Leaving game...");
                    return;
                }
                default -> System.out.println("Invalid input.");
            }
        }
    }

    private void makeMove() {
        try {
            System.out.print("Enter start position (e.g., e2): ");
            String from = scanner.nextLine().trim();
            System.out.print("Enter end position (e.g., e4): ");
            String to = scanner.nextLine().trim();

            ChessPosition start = parsePosition(from);
            ChessPosition end = parsePosition(to);
            ChessMove move = new ChessMove(start, end, null); // You can add promotion later

            ws.sendMove(gameId, move);
        } catch (Exception e) {
            System.out.println("Invalid move format.");
        }
    }

    private ChessPosition parsePosition(String pos) {
        int col = pos.charAt(0) - 'a' + 1;
        int row = Integer.parseInt(pos.substring(1));
        return new ChessPosition(row, col);
    }
}
