package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
    }

    private ChessBoard board;
    private TeamColor currentTurn = TeamColor.WHITE;
    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();


    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null; // obvious
        }

        Collection<ChessMove> legalMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> moves = new ArrayList<>();

        for (ChessMove move : legalMoves){
            ChessPiece captured = board.getPiece(move.getEndPosition());
            ChessPiece moved = board.getPiece(startPosition);

            board.addPiece(move.getEndPosition(), moved);
            board.addPiece(startPosition, null);

            if (!isInCheck(moved.getTeamColor())) {
                moves.add(move); // adds move to list if move is valid, and not in check
            }

            board.addPiece(startPosition, moved); // resets board , piece moved
            board.addPiece(move.getEndPosition(), captured); // resets board , piece captured, if any
        }

        return moves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);

        if (piece == null || piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("No piece at position or wrong team's turn");
        }

        Collection<ChessMove> validMoves = validMoves(start);
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move for piece");
        }

        if (move.getPromotionPiece() != null && piece.getPieceType() == piece.getPieceType().PAWN) {
            ChessPiece promoted = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
            board.addPiece(move.getEndPosition(), promoted);
        } else {
            board.addPiece(move.getEndPosition(), piece);
        }


        board.addPiece(end, new ChessPiece(piece.getTeamColor(), move.getPromotionPiece() != null ? move.getPromotionPiece() : piece.getPieceType()));
        board.addPiece(start, null);

        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    kingPosition = position;

                }
            }
        }
        if (kingPosition == null) {
            return false;
        }
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() != teamColor) {
                    Collection<ChessMove> theirMoves = piece.pieceMoves(board, position);
                    for (ChessMove move : theirMoves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true; // King can be captured
                        }
                    }
                }
            }
        }

        return false;
    }


    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false; // Has a legal move, so not checkmate
                    }
                }
            }
        }

        return true;
    }


    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor))
        {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }

        return true;

    }


    public void setBoard(ChessBoard board) {
        this.board = board;
    }


    public ChessBoard getBoard() {
        return board;

    }
}
