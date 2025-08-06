package ui;

public class ChessClient {

    public static void main(String[] args) {
        new ChessClient().run();
    }

    public void run() {
        System.out.println("Starting Chess Client...");
        PreLoginUI preLoginUI = new PreLoginUI("http://localhost:8080");

        while (true) {
            boolean shouldQuit = preLoginUI.run();
            if (shouldQuit) {
                break;
            }
        }
    }
}
