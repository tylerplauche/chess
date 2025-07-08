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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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
        if (type == PieceType.PAWN) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();


            if(color == ChessGame.TeamColor.BLACK ) {
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
                if (row == 2 && target == null) {
                    moves.add(new ChessMove(myPosition, newPos, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, newPos, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, newPos, PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, newPos, PieceType.BISHOP));
                }
                if (row == 2 && target1 != null && target2.getTeamColor() != this.color) {
                    moves.add(new ChessMove(myPosition, newPos1, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, newPos1, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, newPos1, PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, newPos1, PieceType.BISHOP));
                }
                if (row == 2 && target2 != null && target2.getTeamColor() != this.color) {
                    moves.add(new ChessMove(myPosition, newPos2, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, newPos2, PieceType.ROOK));
                    moves.add(new ChessMove(myPosition, newPos2, PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, newPos2, PieceType.BISHOP));
                }

            }

            if(color == ChessGame.TeamColor.WHITE ) {
                ChessPosition newPos = new ChessPosition(row +1, col);
                ChessPiece target = board.getPiece(newPos);

                ChessPiece target1 = null;
                ChessPosition newPos1 = new ChessPosition(row + 1, col+1);


                if (col < 8) {
                    target1 = board.getPiece(newPos1);
                }


                ChessPiece target2 = null;
                ChessPosition newPos2 = new ChessPosition(row + 1, col-1);


                if (col > 1) {
                    target2 = board.getPiece(newPos2);
                }

                ChessPiece target3 = null;
                ChessPosition newPos3 = new ChessPosition(row + 2, col);


                if (row < 7) {
                    target3 = board.getPiece(newPos3);
                }
                if (row <8) {

                    if (row != 7 && target == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    }

                    if (row != 7 && target1 != null && target1.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, newPos1, null));
                    }

                    if (row != 7 && target2 != null && target2.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, newPos2, null));
                    }

                    if (row == 2 && target3 == null && target == null) {
                        moves.add(new ChessMove(myPosition, newPos3, null));

                    }
                    if (row == 7 && target == null) {
                        moves.add(new ChessMove(myPosition, newPos, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, newPos, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, newPos, PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, newPos, PieceType.BISHOP));
                    }
                    if (row == 7 && target1 != null && target2.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, newPos1, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, newPos1, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, newPos1, PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, newPos1, PieceType.BISHOP));
                    }
                    if (row == 7 && target2 != null && target2.getTeamColor() != this.color) {
                        moves.add(new ChessMove(myPosition, newPos2, PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition, newPos2, PieceType.ROOK));
                        moves.add(new ChessMove(myPosition, newPos2, PieceType.KNIGHT));
                        moves.add(new ChessMove(myPosition, newPos2, PieceType.BISHOP));
                    }

                }


            }


        }
        if (type == PieceType.KNIGHT) {
            int[][] directions = {{-1, 2}, {1, 2}, {1, -2}, {-1, -2}, {2, 1}, {2, -1}, {-2, 1}, {-2, -1}};

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
                        if (target.getTeamColor() == this.color) {
                            break;
                        }
                        if (target.getTeamColor() != this.color) {
                            moves.add(new ChessMove(myPosition, newPos, null));
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
