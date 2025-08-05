package ui;

import chess.*;

import java.util.Map;

public class BoardRenderer {


    private static final Map<ChessPiece.PieceType, String[]> UNICODE = Map.of(
            ChessPiece.PieceType.KING,   new String[] {"♔", "♚"},
            ChessPiece.PieceType.QUEEN,  new String[] {"♕", "♛"},
            ChessPiece.PieceType.ROOK,   new String[] {"♖", "♜"},
            ChessPiece.PieceType.BISHOP, new String[] {"♗", "♝"},
            ChessPiece.PieceType.KNIGHT, new String[] {"♘", "♞"},
            ChessPiece.PieceType.PAWN,   new String[] {"♙", "♟"}
    );


    private static final String LIGHT_BG = "\u001B[47m"; // white background
    private static final String DARK_BG = "\u001B[40m";  // black background
    private static final String WHITE_FG = "\u001B[31m"; // red pieces
    private static final String BLACK_FG = "\u001B[34m"; // blue pieces

    private static final String RESET = "\u001B[0m";

    public static void drawBoard(ChessGame game, boolean isWhitePerspective) {
        ChessBoard board = game.getBoard();

        int rowStart = isWhitePerspective ? 8 : 1;
        int rowEnd = isWhitePerspective ? 0 : 9;
        int rowStep = isWhitePerspective ? -1 : 1;

        int colStart = isWhitePerspective ? 1 : 8;
        int colEnd = isWhitePerspective ? 9 : 0;
        int colStep = isWhitePerspective ? 1 : -1;

        for (int row = rowStart; row != rowEnd; row += rowStep) {
            System.out.print(row + " "); // Row label
            for (int col = colStart; col != colEnd; col += colStep) {
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                boolean lightSquare = (row + col) % 2 != 0;
                String bg = lightSquare ? LIGHT_BG : DARK_BG;

                String symbol = " ";
                if (piece != null) {
                    String[] pieceSymbols = UNICODE.get(piece.getPieceType());
                    symbol = piece.getTeamColor() == ChessGame.TeamColor.WHITE
                            ? WHITE_FG + pieceSymbols[0]
                            : BLACK_FG + pieceSymbols[1];
                }

                System.out.print(bg + " " + symbol + " " + RESET);
            }
            System.out.println();
        }


        System.out.print("  ");
        for (int col = colStart; col != colEnd; col += colStep) {
            char colLabel = (char) ('a' + col - 1);
            System.out.print(" " + colLabel + " ");
        }
        System.out.println();
    }
}
