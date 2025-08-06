package ui;

import chess.ChessGame;
import model.AuthData;
import model.GameData;

import java.util.Collection;
import java.util.Scanner;

public class PostLoginUI {
    private final ServerFacade server;
    private final AuthData auth;
    private final Scanner scanner = new Scanner(System.in);
    private GameData[] listedGames;

    public PostLoginUI(ServerFacade server, AuthData auth) {
        this.server = server;
        this.auth = auth;
    }

    public void run() {
        System.out.println("You are now logged in. Type 'help' to see available commands.");
        while (true) {
            System.out.print("(" + auth.username() + ")> ");
            String input = scanner.nextLine().trim();
            String[] tokens = input.split("\\s+");

            if (tokens.length == 0) {
                continue;
            }

            String command = tokens[0].toLowerCase();

            try {
                switch (command) {
                    case "help" -> printHelp();
                    case "logout" -> {
                        server.logout(auth.authToken());
                        System.out.println("Logged out.");
                        return;
                    }
                    case "create" -> handleCreate(tokens);
                    case "list" -> handleList();
                    case "join" -> handleJoin(tokens);
                    case "observe" -> handleObserve(tokens);
                    default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printHelp() {
        System.out.println("""
            Commands:
              help                             - Show available commands
              create <GAME_NAME>               - Create a new game
              list                             - List available games
              join <GAME_NUMBER> [White|Black] - Join a game by number and color
              observe <GAME_NUMBER>            - Observe a game by number
              logout                           - Log out
        """);
    }

    private void handleCreate(String[] tokens) throws Exception {
        if (tokens.length != 2) {
            System.out.println("Usage: create <GAME_NAME>");
            return;
        }
        server.createGame(auth.authToken(), tokens[1]);
        System.out.println("Game '" + tokens[1] + "' created.");
    }

    private void handleList() throws Exception {
        Collection<GameData> games = server.listGames(auth.authToken());
        listedGames = games.toArray(new GameData[0]);

        System.out.println("Available Games:");
        for (int i = 0; i < listedGames.length; i++) {
            GameData game = listedGames[i];
            System.out.printf("  %d. %s | White: %s | Black: %s%n",
                    i + 1,
                    game.gameName(),
                    game.whiteUsername() != null ? game.whiteUsername() : "-",
                    game.blackUsername() != null ? game.blackUsername() : "-");
        }
    }

    private void handleJoin(String[] tokens) throws Exception {
        if (tokens.length < 2 || tokens.length > 3) {
            System.out.println("Usage: join <GAME_NUMBER> [White|Black]");
            return;
        }

        int gameNum = Integer.parseInt(tokens[1]);
        if (gameNum < 1 || gameNum > listedGames.length) {
            System.out.println("Invalid game number.");
            return;
        }

        String color = (tokens.length == 3) ? tokens[2].toUpperCase() : null;
        GameData selectedGame = listedGames[gameNum - 1];
        server.joinGame(auth.authToken(), selectedGame.gameID(), color);
        System.out.printf("Joined game '%s' as %s.%n", selectedGame.gameName(),
                (color == null ? "observer" : color));

        // Step 1: Construct WebSocketFacade
        WebSocketFacade ws = new WebSocketFacade();

// Step 2: Create the GameplayUI, passing in ws, gameId, username, and color
        GameplayUI gameplay = new GameplayUI(ws, selectedGame.gameID(), auth.username(), color);

// Step 3: Connect the WebSocket and start the UI
        try {
            ws.connect("ws://localhost:8080/connect", gameplay::handleMessage);
            ws.sendJoin(selectedGame.gameID(), color);
            gameplay.run();
        } catch (Exception e) {
            System.out.println("WebSocket connection failed: " + e.getMessage());
        }

    }

    private void handleObserve(String[] tokens) throws Exception {
        String color = null;
        if (tokens.length != 2) {
            System.out.println("Usage: observe <GAME_NUMBER>");
            return;
        }

        int gameNum = Integer.parseInt(tokens[1]);
        if (gameNum < 1 || gameNum > listedGames.length) {
            System.out.println("Invalid game number.");
            return;
        }

        GameData selectedGame = listedGames[gameNum - 1];
        server.joinGame(auth.authToken(), selectedGame.gameID(), null);
        System.out.printf("Observing game '%s'.%n", selectedGame.gameName());

        WebSocketFacade ws = new WebSocketFacade();

        GameplayUI gameplay = new GameplayUI(ws, selectedGame.gameID(), auth.username(), color);

        try {
            ws.connect("ws://localhost:8080/connect", gameplay::handleMessage);
            ws.sendJoin(selectedGame.gameID(), color);
            gameplay.run();
        } catch (Exception e) {
            System.out.println("WebSocket connection failed: " + e.getMessage());
        }

    }
}

