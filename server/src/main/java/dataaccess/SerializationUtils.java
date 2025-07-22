package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;

public class SerializationUtils {
    private static final Gson gson = new Gson();

    public static String serializeGame(ChessGame game) {
        return gson.toJson(game);
    }

    public static ChessGame deserializeGame(String json) {
        return gson.fromJson(json, ChessGame.class);
    }
}
