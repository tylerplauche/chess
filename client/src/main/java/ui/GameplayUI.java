package ui;

import chess.*;

import model.WebSocketMessage;
import java.util.Scanner;

public class GameplayUI {
    private final Scanner scanner = new Scanner(System.in);
    private final WebSocketFacade ws;
    private final int gameId;
    private final String username;
    private final String playerColor;
    private ChessGame gameState = new ChessGame();

    public GameplayUI(WebSocketFacade ws, int gameId, String username, String playerColor) {
        this.ws = ws;
        this.gameId = gameId;
        this.username = username;
        this.playerColor = playerColor;
    }

    public void run() {
        System.out.println("\nJoined game " + gameId + " as " + playerColor + ".");

        try {
            ws.connect("ws://localhost:8080/connect", this::handleMessage);
            ws.sendJoin(gameId, playerColor);
        } catch (Exception e) {
            System.out.println("WebSocket connection failed: " + e.getMessage());
            return;
        }

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
                    ws.sendResign(gameId);
                    System.out.println("You resigned.");
                    return;
                }
                case "3" -> printBoard();
                case "4" -> {
                    System.out.println("Exiting game...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
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

            ChessMove move = new ChessMove(start, end, null); // TODO: add promotion if needed
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

    public void handleMessage(WebSocketMessage message) {
        String type = message.getServerMessageType();
        if (type == null) {
            System.out.println("Received null message type from server.");
            return;
        }

        switch (type) {
            case "LOAD_GAME":
                ChessGame loadedGame = message.getGame();
                if (loadedGame != null) {
                    this.gameState = loadedGame;
                    printBoard();
                } else {
                    System.out.println("Error: received LOAD_GAME but game is null.");
                }
                break;

            case "ERROR":
                System.out.println("Server error: " + message.getErrorMessage());
                break;

            case "NOTIFICATION":
                System.out.println("Server: " + message.getMessage());
                break;

            default:
                System.out.println("Unknown server message type: " + type);
        }
    }


    private void printBoard() {
        boolean isWhite = playerColor.equalsIgnoreCase("WHITE");
        BoardRenderer.drawBoard(gameState, isWhite);
    }
}
