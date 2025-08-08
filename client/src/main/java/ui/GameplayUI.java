package ui;

import chess.*;
import websocket.commands.*;
import websocket.messages.*;

import java.util.Scanner;

public class GameplayUI {
    private final Scanner scanner = new Scanner(System.in);
    private final WebSocketFacade ws;
    private final int gameId;
    private final String username;
    private final String playerColor;
    private ChessGame gameState = new ChessGame();
    private final String websocketUrl;
    private final String authToken;



    public GameplayUI(String websocketUrl, int gameId, String username, String playerColor, String authToken) {
        this.websocketUrl = websocketUrl;
        this.ws = new WebSocketFacade();
        this.gameId = gameId;
        this.username = username;
        this.playerColor = playerColor;
        this.authToken = authToken;
    }


    public void run() {
        System.out.println(" Joined game " + gameId + " as " + playerColor + ".");

        try {
            ws.connect(websocketUrl, this::handleMessage);
            sendJoinCommand();
        } catch (Exception e) {
            System.out.println(" Failed to connect to WebSocket: " + e.getMessage());
            return;
        }

        gameLoop();
    }

    private void sendJoinCommand() {
        try {
            ChessGame.TeamColor color = ChessGame.TeamColor.valueOf(playerColor.toUpperCase());
            JoinPlayer joinCommand = new JoinPlayer(authToken, username, gameId, color);
            ws.send(joinCommand);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid team color: " + playerColor);
        }
    }

    private void gameLoop() {
        while (true) {
            System.out.println("\n--- Game Menu ---");
            System.out.println("1. Make Move");
            System.out.println("2. Resign");
            System.out.println("3. Refresh Board");
            System.out.println("4. Leave Game");
            System.out.print("Select an option: ");

            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> makeMove();
                case "2" -> {
                    Resign resignCommand = new Resign(username, authToken, gameId);

                    ws.send(resignCommand);
                    System.out.println("You resigned.");
                    return;
                }
                case "3" -> printBoard();
                case "4" -> {
                    System.out.println("Exiting game...");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
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

            ChessMove move = new ChessMove(start, end, null);
            MakeMove moveCommand = new MakeMove(username, gameId, move);
            ws.send(moveCommand);
        } catch (Exception e) {
            System.out.println("⚠ Invalid move format. Use notation like 'e2' and 'e4'.");
        }
    }

    private ChessPosition parsePosition(String pos) {
        if (pos.length() != 2) {
            throw new IllegalArgumentException("Invalid position format");
        }
        int col = pos.toLowerCase().charAt(0) - 'a' + 1;
        int row = Character.getNumericValue(pos.charAt(1));
        return new ChessPosition(row, col);
    }

    private void handleMessage(ServerMessage message) {
        if (message == null) {
            System.out.println(" Received null message");
            return;
        }

        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGame load = (LoadGame) message;
                this.gameState = load.getGame();
                printBoard();
            }
            case NOTIFICATION -> {
                Notification notification = (Notification) message;
                System.out.println("🔔 " + notification.getMessage());
            }
            case ERROR -> {
                ErrorMessage error = (ErrorMessage) message;
                System.out.println(" Server error: " + error.getErrorMessage());
            }
            default -> System.out.println("Unknown message type received.");
        }
    }

    private void printBoard() {
        boolean isWhite = playerColor.equalsIgnoreCase("WHITE");
        BoardRenderer.drawBoard(gameState, isWhite);
    }
}
