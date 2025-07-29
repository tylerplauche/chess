package ui;

import model.AuthData;

public class ChessClient {

    public static void main(String[] args) {
        new ChessClient().run();
    }

    public void run() {
        System.out.println("Starting Chess Client...");

        while (true) {

            PreLoginUI preLoginUI = new PreLoginUI("http://localhost:8080");
            preLoginUI.run();

        }
    }
}
