package client;

import model.LoginRequest;
import model.RegisterRequest;
import model.RegisterResult;
import model.LoginResult;

import java.util.Scanner;

public class CommandHandler {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ServerFacade server = new ServerFacade("http://localhost:8080");

    public static void handlePreLogin(String input, SessionContext session) {
        switch (input.toLowerCase()) {
            case "help" -> printPreLoginHelp();
            case "register" -> handleRegister(session);
            case "login" -> handleLogin(session);
            default -> System.out.println("Unknown command. Type 'help' to see available commands.");
        }
    }

    public static void handlePostLogin(String input, SessionContext session) {
        switch (input.toLowerCase()) {
            case "help" -> printPostLoginHelp();
            case "logout" -> {
                session.logout();
                System.out.println("Logged out.");
            }
            default -> System.out.println("Unknown command. Type 'help' to see available commands.");
        }
    }

    private static void handleRegister(SessionContext session) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Email (optional): ");
        String email = scanner.nextLine().trim();

        try {
            RegisterRequest request = new RegisterRequest(username, password, email);
            RegisterResult result = server.register(request);
            session.login(result.getUsername(), result.getAuthToken());
            System.out.println("Registered and logged in as " + result.getUsername());
        } catch (Exception e) {
            System.out.println("Register failed: " + e.getMessage());
        }
    }

    private static void handleLogin(SessionContext session) {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            LoginRequest request = new LoginRequest(username, password);
            LoginResult result = server.login(request);
            session.login(result.getUsername(), result.getAuthToken());
            System.out.println("Logged in as " + result.getUsername());
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private static void printPreLoginHelp() {
        System.out.println("Commands:");
        System.out.println("  register - Register a new user");
        System.out.println("  login    - Login with your credentials");
        System.out.println("  help     - Show this help message");
        System.out.println("  quit     - Exit the application");
    }

    private static void printPostLoginHelp() {
        System.out.println("Commands:");
        System.out.println("  create game <name>  - Create a new game");
        System.out.println("  list games          - List all available games");
        System.out.println("  play <id> <color>   - Join a game as white/black");
        System.out.println("  observe <id>        - Observe a game");
        System.out.println("  logout              - Logout from the session");
        System.out.println("  help                - Show this help message");
        System.out.println("  quit                - Exit the application");
    }
}
