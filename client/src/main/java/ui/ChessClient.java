package ui;

public class ChessClient {
    public static void main(String[] args) {
        String serverUrl = (args.length > 0) ? args[0] : null;

        if (serverUrl == null) {
            serverUrl = System.getenv("CHESS_SERVER_URL");
        }

        if (serverUrl == null) {
            serverUrl = System.getProperty("chess.server.url");
        }

        if (serverUrl == null) {
            serverUrl = "http://localhost:8080";
        }

        new ChessClient().run(serverUrl);
    }

    public void run(String serverUrl) {
        System.out.println("Starting Chess Client with server: " + serverUrl);
        PreLoginUI preLoginUI = new PreLoginUI(serverUrl);

        while (true) {
            boolean shouldQuit = preLoginUI.run();
            if (shouldQuit) {
                break;
            }
        }
    }
}
