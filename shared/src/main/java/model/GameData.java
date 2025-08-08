package model;

import chess.ChessGame;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {


    @Override
    public ChessGame game() {
        return game;
    }

    @Override
    public String gameName() {
        return gameName;
    }

    @Override
    public String blackUsername() {
        return blackUsername;
    }

    @Override
    public String whiteUsername() {
        return whiteUsername;
    }

    @Override
    public int gameID() {
        return gameID;
    }
}
