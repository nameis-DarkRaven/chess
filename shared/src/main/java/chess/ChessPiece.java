package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


public class ChessPiece {
    private ChessGame.TeamColor color;
    private PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options.
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    public PieceType getPieceType() {
        return type;
    }


    /**
     * Calculates all the positions a chess piece can move to.
     * Does not take into account moves that are illegal due to leaving the king in danger.
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (this.type) {
            case PieceType.KING -> kingMoves(board, myPosition);
            case PieceType.QUEEN -> queenMoves(board, myPosition);
            case PieceType.BISHOP -> bishopMoves(board, myPosition);
            case PieceType.KNIGHT -> knightMoves(board, myPosition);
            case PieceType.ROOK -> rookMoves(board, myPosition);
            case PieceType.PAWN -> pawnMoves(board, myPosition);
        };
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        int row = myPosition.getRow(), column = myPosition.getColumn();
        List<ChessMove> possibleMoves = new ArrayList<>();
        addMove(row + 1, column, possibleMoves, myPosition, board, null); /* backward */
        addMove(row - 1, column, possibleMoves, myPosition, board, null); /* forward */
        addMove(row, column + 1, possibleMoves, myPosition, board, null); /* right */
        addMove(row, column - 1, possibleMoves, myPosition, board, null); /* left */
        addMove(row + 1, column + 1, possibleMoves, myPosition, board, null); /* backward right */
        addMove(row + 1, column - 1, possibleMoves, myPosition, board, null); /* backward left */
        addMove(row - 1, column + 1, possibleMoves, myPosition, board, null); /* forward right */
        addMove(row - 1, column - 1, possibleMoves, myPosition, board, null); /* forward left */
        return possibleMoves;
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        possibleMoves.addAll(bishopMoves(board, myPosition));
        possibleMoves.addAll(rookMoves(board, myPosition));
        return possibleMoves;
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        checkMove(1, -1, myPosition, possibleMoves, board); /* backward left */
        checkMove(-1, -1, myPosition, possibleMoves, board); /* forward left */
        checkMove(1, 1, myPosition, possibleMoves, board); /* backward right */
        checkMove(-1, 1, myPosition, possibleMoves, board); /* forward right */
        return possibleMoves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow(), column = myPosition.getColumn();
        addMove(row + 2, column + 1, possibleMoves, myPosition, board, null); /* backward two right one */
        addMove(row + 2, column - 1, possibleMoves, myPosition, board, null); /* backward two left one */
        addMove(row + 1, column + 2, possibleMoves, myPosition, board, null); /* backward one right two */
        addMove(row + 1, column - 2, possibleMoves, myPosition, board, null); /* backward one left two */
        addMove(row - 2, column + 1, possibleMoves, myPosition, board, null); /* forward two right one */
        addMove(row - 2, column - 1, possibleMoves, myPosition, board, null); /* forward two left one */
        addMove(row - 1, column + 2, possibleMoves, myPosition, board, null); /* forward one right two */
        addMove(row - 1, column - 2, possibleMoves, myPosition, board, null); /* forward one left two */
        return possibleMoves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        checkMove(0, -1, myPosition, possibleMoves, board); /* left */
        checkMove(0, 1, myPosition, possibleMoves, board); /* right */
        checkMove(1, 0, myPosition, possibleMoves, board); /* backward */
        checkMove(-1, 0, myPosition, possibleMoves, board); /* forward */
        return possibleMoves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow(), column = myPosition.getColumn();
        if (this.color == ChessGame.TeamColor.WHITE) {
            if (row == 2) {
                addMove(row + 1, column, possibleMoves, myPosition, board, null);
                addPawnMove(row + 1, column + 1, possibleMoves, myPosition, board, null);
                addPawnMove(row + 1, column - 1, possibleMoves, myPosition, board, null);
                if (board.getPiece(new ChessPosition(row + 1, column)) == null) {
                    addMove(row + 2, column, possibleMoves, myPosition, board, null);
                }
            } else if (row == 7) {
                addMove(row + 1, column, possibleMoves, myPosition, board, PieceType.QUEEN);
                addMove(row + 1, column, possibleMoves, myPosition, board, PieceType.BISHOP);
                addMove(row + 1, column, possibleMoves, myPosition, board, PieceType.KNIGHT);
                addMove(row + 1, column, possibleMoves, myPosition, board, PieceType.ROOK);
                addPawnCapturePromotionMove(row + 1, column + 1, possibleMoves, myPosition, board);
                addPawnCapturePromotionMove(row + 1, column - 1, possibleMoves, myPosition, board);
            } else {
                addMove(row + 1, column, possibleMoves, myPosition, board, null);
                addPawnMove(row + 1, column + 1, possibleMoves, myPosition, board, null);
                addPawnMove(row + 1, column - 1, possibleMoves, myPosition, board, null);
            }


        }
        if (this.color == ChessGame.TeamColor.BLACK) {
            if (row == 7) {
                addMove(row - 1, column, possibleMoves, myPosition, board, null);
                addPawnMove(row - 1, column + 1, possibleMoves, myPosition, board, null);
                addPawnMove(row - 1, column - 1, possibleMoves, myPosition, board, null);
                if (board.getPiece(new ChessPosition(row - 1, column)) == null) {
                    addMove(row - 2, column, possibleMoves, myPosition, board, null);
                }
            } else if (row == 2) {
                addMove(row - 1, column, possibleMoves, myPosition, board, PieceType.QUEEN);
                addMove(row - 1, column, possibleMoves, myPosition, board, PieceType.BISHOP);
                addMove(row - 1, column, possibleMoves, myPosition, board, PieceType.KNIGHT);
                addMove(row - 1, column, possibleMoves, myPosition, board, PieceType.ROOK);
                addPawnCapturePromotionMove(row - 1, column + 1, possibleMoves, myPosition, board);
                addPawnCapturePromotionMove(row - 1, column - 1, possibleMoves, myPosition, board);
            } else {
                addMove(row - 1, column, possibleMoves, myPosition, board, null);
                addPawnMove(row - 1, column + 1, possibleMoves, myPosition, board, null);
                addPawnMove(row - 1, column - 1, possibleMoves, myPosition, board, null);
            }
        }
        return possibleMoves;
    }

    /**
     * A method to add a move to a piece's possible moves.
     * Used for all pieces except diagonal pawn moves.
     */
    private void addMove(int row, int col, Collection<ChessMove> moves, ChessPosition position, ChessBoard board, ChessPiece.PieceType piece) {
        ChessPosition newPosition = new ChessPosition(row, col);
        if (inBoardRange(newPosition) && board.getPiece(newPosition) == null) {
            moves.add(new ChessMove(position, newPosition, piece));
        } else if (inBoardRange(newPosition) && board.getPiece(newPosition).color != this.color && this.type != PieceType.PAWN) {
            moves.add(new ChessMove(position, newPosition, piece));
        }
    }

    /**
     * A method to add a diagonal pawn move to a pawn's possible moves.
     */
    private void addPawnMove(int row, int col, Collection<ChessMove> moves, ChessPosition position, ChessBoard board, ChessPiece.PieceType piece) {
        ChessPosition newPosition = new ChessPosition(row, col);
        if (inBoardRange(newPosition) && board.getPiece(newPosition) != null && board.getPiece(newPosition).color != this.color) {
            moves.add(new ChessMove(position, newPosition, piece));
        }
    }

    /**
     * A method to add promotion moves to a pawn's possible moves.
     */
    private void addPawnCapturePromotionMove(int row, int column, Collection<ChessMove> moves, ChessPosition myPosition, ChessBoard board) {
        ChessPosition newPosition = new ChessPosition(row, column);
        if ((board.getPiece(newPosition) != null && board.getPiece(newPosition).color != this.color)) {
            addPawnMove(row, column, moves, myPosition, board, PieceType.QUEEN);
            addPawnMove(row, column, moves, myPosition, board, PieceType.BISHOP);
            addPawnMove(row, column, moves, myPosition, board, PieceType.ROOK);
            addPawnMove(row, column, moves, myPosition, board, PieceType.KNIGHT);
        }
    }

    /**
     * Checks to see if a range of moves is possible.
     * Used for bishop and rook moves.
     */
    private void checkMove(int rowMove, int columnMove, ChessPosition myPosition, Collection<ChessMove> possibleMoves, ChessBoard board) {
        int row = myPosition.getRow(), column = myPosition.getColumn();
        boolean isPiece = false;
        while (inBoardRange(row, column) && !isPiece) {
            row += rowMove;
            column += columnMove;
            if (inBoardRange(row, column)) {
                addMove(row, column, possibleMoves, myPosition, board, null);
                if (board.getPiece(new ChessPosition(row, column)) != null) {
                    isPiece = true;
                }
            }
        }
    }

    /**
     * Two of the same method with different parameters for convenience.
     * Says whether a move would be within the range of the board.
     */
    private Boolean inBoardRange(int row, int column) {
        return row <= 8 && row > 0 && column <= 8 && column > 0;
    }

    private Boolean inBoardRange(ChessPosition position) {
        int row = position.getRow();
        int column = position.getColumn();
        return row <= 8 && row > 0 && column <= 8 && column > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
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

    @Override
    public String toString() {
        return "ChessPiece{" +
                "color=" + color +
                ", type=" + type +
                '}';
    }
}
