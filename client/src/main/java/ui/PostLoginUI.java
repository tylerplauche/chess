package ui;

import model.AuthData;
import model.GameData;

import java.util.Collection;
import java.util.Scanner;

public class PostLoginUI {
    private final ServerFacade server;
    private final AuthData auth;
    private final Scanner scanner = new Scanner(System.in);

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
                  create <GAME_NAME>       - Create a new game
                  list                     - List available games
                  join <GAME_ID> [WHITE|BLACK|<empty>] - Join a game as white/black/observe
                  logout                   - Log out
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
        System.out.println("Available Games:");
        for (GameData game : games) {
            System.out.printf("  ID: %d | Name: %s | White: %s | Black: %s%n",
                    game.gameID(),
                    game.gameName(),
                    game.whiteUsername() != null ? game.whiteUsername() : "-",
                    game.blackUsername() != null ? game.blackUsername() : "-");
        }
    }

    private void handleJoin(String[] tokens) throws Exception {
        if (tokens.length < 2 || tokens.length > 3) {
            System.out.println("Usage: join <GAME_ID> [WHITE|BLACK]");
            return;
        }
        int gameId = Integer.parseInt(tokens[1]);
        String playerColor = tokens.length == 3 ? tokens[2].toUpperCase() : "";

        // Placeholder - game UI coming later
        System.out.printf("Pretending to join game %d as %s (not implemented yet).%n",
                gameId, playerColor.isEmpty() ? "observer" : playerColor);
    }
}
