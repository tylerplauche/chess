package ui;

import model.AuthData;
import java.util.Scanner;

public class ChessClient {
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade = new ServerFacade("http://localhost:8080"); // change port if needed
    private AuthData currentAuth;

    public void run() {
        System.out.println("Welcome to Chess!");
        while (true) {
            if (currentAuth == null) {
                showPreloginPrompt();
            } else {
                showPostloginPrompt();
            }
        }
    }

    private void showPreloginPrompt() {
        System.out.print("\n[Not logged in] > ");
        String input = scanner.nextLine().trim().toLowerCase();

        switch (input) {
            case "help" -> showPreloginHelp();
            case "quit" -> quit();
            case "register" -> handleRegister();
            case "login" -> handleLogin();
            default -> System.out.println("Unknown command. Type 'help' for a list.");
        }
    }

    private void showPostloginPrompt() {
        System.out.print("\n[Logged in] > ");
        String input = scanner.nextLine().trim().toLowerCase();

        switch (input) {
            case "help" -> showPostloginHelp();
            case "logout" -> handleLogout();
            case "create game" -> handleCreateGame();
            case "list games" -> handleListGames();
            // We'll add play/observe later
            default -> System.out.println("Unknown command. Type 'help' for a list.");
        }
    }

    private void showPreloginHelp() {
        System.out.println("Available commands:");
        System.out.println("- register");
        System.out.println("- login");
        System.out.println("- help");
        System.out.println("- quit");
    }

    private void showPostloginHelp() {
        System.out.println("Available commands:");
        System.out.println("- logout");
        System.out.println("- create game");
        System.out.println("- list games");
        System.out.println("- help");
        System.out.println("- quit");
    }

    private void quit() {
        System.out.println("Goodbye!");
        System.exit(0);
    }

    private void handleRegister() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        try {
            currentAuth = facade.register(username, password, email);
            System.out.println("Registered and logged in as " + username);
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            currentAuth = facade.login(username, password);
            System.out.println("Logged in as " + username);
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private void handleLogout() {
        try {
            facade.logout(currentAuth.authToken());
            currentAuth = null;
            System.out.println("Logged out.");
        } catch (Exception e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
    }

    private void handleCreateGame() {
        System.out.print("Game name: ");
        String gameName = scanner.nextLine().trim();
        try {
            facade.createGame(currentAuth.authToken(), gameName);
            System.out.println("Game '" + gameName + "' created.");
        } catch (Exception e) {
            System.out.println("Failed to create game: " + e.getMessage());
        }
    }

    private void handleListGames() {
        try {
            var games = facade.listGames(currentAuth.authToken());
            if (games.isEmpty()) {
                System.out.println("No games available.");
            } else {
                int index = 1;
                for (var game : games) {
                    System.out.printf("%d. %s (White: %s, Black: %s)%n", index++, game.gameName(), game.whiteUsername(), game.blackUsername());
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to list games: " + e.getMessage());
        }
    }
}
