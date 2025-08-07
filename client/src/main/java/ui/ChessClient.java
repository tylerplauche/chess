package ui;

public class ChessClient {
    public static void main(String[] args) {
        String serverUrl = (args.length > 0) ? args[0] : "http://localhost:8080";
        new ChessClient().run(serverUrl);
    }

    public void run(String serverUrl) {
        System.out.println("Starting Chess Client...");
        PreLoginUI preLoginUI = new PreLoginUI(serverUrl);

        while (true) {
            boolean shouldQuit = preLoginUI.run();
            if (shouldQuit) {
                break;
            }
        }
    }
}

