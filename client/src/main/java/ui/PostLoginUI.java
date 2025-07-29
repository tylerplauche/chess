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

            if (tokens.length == 0) continue;

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
          create <GAME_NAME>             - Create a new game
          list                           - List available games
          join <GAME_ID> [WHITE|BLACK]   - Join a game by ID and color
                                          - Leave color blank to observe
          logout                         - Log out
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
            System.out.println("Usage: join <GAME_ID> [WHITE|BLACK]");
            System.out.println("Leave color blank to observe the game.");
            return;
        }

        int gameID;
        try {
            gameID = Integer.parseInt(tokens[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID.");
            return;
        }

        String color = (tokens.length == 3) ? tokens[2].toUpperCase() : null;
        server.joinGame(auth.authToken(), gameID, color);
        System.out.printf("Joined game ID %d as %s.%n", gameID, (color == null ? "observer" : color));

        boolean whitePerspective = !"BLACK".equalsIgnoreCase(color);
        ChessGame game = new ChessGame(); // Replace with actual game state fetch if needed
        BoardRenderer.drawBoard(game, whitePerspective);
    }



}
