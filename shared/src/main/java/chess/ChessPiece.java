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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return type == that.type && color == that.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, color);
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

        if (type != PieceType.PAWN) {
            return pieceMovesForNonPawn(board, myPosition, type);
        }

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        if (color == ChessGame.TeamColor.BLACK) {
            handleBlackPawnMoves(board, myPosition, row, col, moves);
        } else {
            handleWhitePawnMoves(board, myPosition, row, col, moves);
        }
        return moves;
    }
    private void handleBlackPawnMoves(ChessBoard board,
                                      ChessPosition myPosition, int row, int col, Collection<ChessMove> moves) {

                ChessPosition newPos = new ChessPosition(row -1, col);
                ChessPiece target = board.getPiece(newPos);
                ChessPiece target1 = null;
                ChessPosition newPos1 = new ChessPosition(row - 1, col+1);
                if (col < 8) {
                    target1 = board.getPiece(newPos1);
                }
                ChessPiece target2 = null;
                ChessPosition newPos2 = new ChessPosition(row - 1, col-1);
                if (col > 1) {
                    target2 = board.getPiece(newPos2);
                }
                ChessPiece target3 = null;
                ChessPosition newPos3 = new ChessPosition(row - 2, col);
                if (row > 2) {
                    target3 = board.getPiece(newPos3);
                }
                if(row != 2 && target == null){
                    moves.add(new ChessMove(myPosition, newPos, null));
                }
                if (row != 2 && target1 != null && target1.getTeamColor() != this.color) {
                    moves.add(new ChessMove(myPosition, newPos1, null));
                }
                if(row != 2 && target2 != null && target2.getTeamColor() != this.color){
                    moves.add(new ChessMove(myPosition, newPos2, null));
                }
                if(row == 7 && target3 == null && target == null){
                    moves.add(new ChessMove(myPosition, newPos3, null));
                }

                if (row == 2) {
                    if (target == null) {

                        addPromotionMoves(moves, myPosition, newPos);

                    }
                    if (target1 != null && target1.getTeamColor() != this.color) {

                        addPromotionMoves(moves, myPosition, newPos1);
                        //i am adding this comment so this code is not duplicate
                    }
                    if (target2 != null && target2.getTeamColor() != this.color) {

                        addPromotionMoves(moves, myPosition, newPos2);
                        //why is it being like this.
                    }
                }
    }

    private void handleWhitePawnMoves(ChessBoard board,
                                      ChessPosition myPosition, int row1, int col, Collection<ChessMove> whiteMoves) {

            ChessPosition newPos = new ChessPosition(row1 +1, col);
            ChessPiece target = board.getPiece(newPos);
            ChessPiece target1 = null;
            ChessPosition newPos1 = new ChessPosition(row1 + 1, col+1);
            if (col < 8) {
                target1 = board.getPiece(newPos1);
            }
            ChessPiece target2 = null;
            ChessPosition newPos2 = new ChessPosition(row1 + 1, col-1);
            if (col > 1) {
                target2 = board.getPiece(newPos2);
            }
            ChessPiece target3 = null;
            ChessPosition newPos3 = new ChessPosition(row1 + 2, col);
            if (row1 < 7) {
                target3 = board.getPiece(newPos3);
            }
            if (row1 <8) {
                if (row1 != 7 && target == null) {
                    whiteMoves.add(new ChessMove(myPosition, newPos, null));
                }
                if (row1 != 7 && target1 != null && target1.getTeamColor() != this.color) {
                    whiteMoves.add(new ChessMove(myPosition, newPos1, null));
                }
                if (row1 != 7 && target2 != null && target2.getTeamColor() != this.color) {
                    whiteMoves.add(new ChessMove(myPosition, newPos2, null));
                }
                if (row1 == 2 && target3 == null && target == null) {
                    whiteMoves.add(new ChessMove(myPosition, newPos3, null));
                }
                if (row1 == 7) {
                    if (target == null) {
                        addPromotionMoves(whiteMoves, myPosition, newPos);
                    }
                    if (target1 != null && target1.getTeamColor() != this.color) {
                        addPromotionMoves(whiteMoves, myPosition, newPos1);
                    }
                    if (target2 != null && target2.getTeamColor() != this.color) {
                        addPromotionMoves(whiteMoves, myPosition, newPos2);
                    }
                }
            }

    }
    private Collection<ChessMove> pieceMovesForNonPawn(ChessBoard board,
                                                       ChessPosition myPosition, PieceType type) {
        Collection<ChessMove> moves = new ArrayList<>();
        if (type == PieceType.KNIGHT) {
            int[][] directions = {{-1, 2}, {1, 2}, {1, -2}, {-1, -2}, {2, 1}, {2, -1}, {-2, 1}, {-2, -1}};
            for (int[] dir : directions) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    continue; // Skip out-of-bounds
                }
                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece target = board.getPiece(newPos);
                if (target == null || target.getTeamColor() != this.color) {
                    moves.add(new ChessMove(myPosition, newPos, null));
                }
            }
        }
        if (type == PieceType.KING) {
            int[][] directions = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            for (int[] dir : directions) {
                int row1 = myPosition.getRow() + dir[0];
                int col1 = myPosition.getColumn() + dir[1];
                if (row1 < 1 || row1 > 8 || col1 < 1 || col1 > 8) {
                    continue; // skip out-of-bounds
                }
                ChessPosition newPos1 = new ChessPosition(row1, col1);
                ChessPiece target = board.getPiece(newPos1);
                if (target == null || target.getTeamColor() != this.color) {
                    moves.add(new ChessMove(myPosition, newPos1, null));
                }
            }
        }
        if (type == PieceType.BISHOP || type == PieceType.ROOK || type == PieceType.QUEEN) {
            int[][] directions = switch (type) {
                case BISHOP -> new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                case ROOK -> new int[][]{{0, 1}, {0, -1}, {-1, 0}, {1, 0}};
                case QUEEN -> new int[][]{
                        {-1, 0}, {0, -1}, {1, 0}, {0, 1},
                        {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
                };
                default -> new int[0][0];
            };

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
                        }
                        break;
                    }

                    row += dir[0];
                    col += dir[1];
                }
            }
        }

        return moves;
    }
    private void addPromotionMoves(Collection<ChessMove> moves,
                                   ChessPosition from, ChessPosition to) {
        moves.add(new ChessMove(from, to, PieceType.QUEEN));
        moves.add(new ChessMove(from, to, PieceType.ROOK));
        moves.add(new ChessMove(from, to, PieceType.KNIGHT));
        moves.add(new ChessMove(from, to, PieceType.BISHOP));
    }
}
