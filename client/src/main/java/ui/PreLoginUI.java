package ui;

import model.AuthData;

import java.util.Scanner;

public class PreLoginUI {
    private final ServerFacade server;
    private final Scanner scanner = new Scanner(System.in);

    public PreLoginUI(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public boolean run() {
        System.out.println("Welcome to Chess! Type help to begin.");
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            String[] tokens = input.split("\\s+");

            if (tokens.length == 0) continue;

            String command = tokens[0].toLowerCase();

            try {
                switch (command) {
                    case "help" -> printHelp();
                    case "quit" -> {
                        System.out.println("Goodbye!");
                        return true; // Signal quit
                    }
                    case "register" -> handleRegister(tokens);
                    case "login" -> handleLogin(tokens);
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
                  register <USERNAME> <PASSWORD> <EMAIL> - Register a new user
                  login <USERNAME> <PASSWORD>           - Log in as an existing user
                  quit                                   - Exit the program
                """);
    }

    private void handleRegister(String[] tokens) throws Exception {
        if (tokens.length != 4) {
            System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
            return;
        }
        AuthData auth = server.register(tokens[1], tokens[2], tokens[3]);
        System.out.println("Registered and logged in as: " + auth.username());
        new PostLoginUI(server, auth).run();
    }

    private void handleLogin(String[] tokens) throws Exception {
        if (tokens.length != 3) {
            System.out.println("Usage: login <USERNAME> <PASSWORD>");
            return;
        }
        AuthData auth = server.login(tokens[1], tokens[2]);
        System.out.println("Logged in as: " + auth.username());
        new PostLoginUI(server, auth).run();
    }
}
