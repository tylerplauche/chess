package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    private final ChessGame.TeamColor color;
    private final PieceType type;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        if (type == PieceType.KING) {
            int[][] directions = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

            for (int[] dir : directions) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];


                if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece target = board.getPiece(newPos);

                    if (target == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    } else {
                        if (target.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, newPos, null));

                        }
                        if (target.getTeamColor() == this.color) {

                        }
                    }
                    row += dir[0];
                    col += dir[1];
                }

            }
        }
        if (type == PieceType.BISHOP) {
            int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

            for (int[] dir : directions) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece target = board.getPiece(newPos);

                    if (target == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    } else {
                        if (target.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, newPos, null));
                            break;
                        }
                        if( target.getTeamColor() == this.color){
                            break;
                        }
                    }

                    row += dir[0];
                    col += dir[1];
                }
            }
        }
        if (type == PieceType.ROOK) {
            int[][] directions = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}};

            for (int[] dir : directions) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece target = board.getPiece(newPos);

                    if (target == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    } else {
                        if (target.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, newPos, null));
                            break;
                        }
                        if( target.getTeamColor() == this.color){
                            break;
                        }
                    }

                    row += dir[0];
                    col += dir[1];
                }
            }
        }
        if (type == PieceType.QUEEN) {
            int[][] directions = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            for (int[] dir : directions) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece target = board.getPiece(newPos);

                    if (target == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    } else {
                        if (target.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, newPos, null));
                            break;
                        }
                        if (target.getTeamColor() == this.color) {
                            break;
                        }
                    }

                    row += dir[0];
                    col += dir[1];
                }
            }
        }
        return moves;
    }
}
