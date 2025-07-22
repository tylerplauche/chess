package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;

public class SerializationUtils {
    private static final Gson GSON = new Gson();

    public static String serializeGame(ChessGame game) {
        return GSON.toJson(game);
    }

    public static ChessGame deserializeGame(String json) {
        return GSON.fromJson(json, ChessGame.class);
    }
}
