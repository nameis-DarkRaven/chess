package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor color;
    private PieceType type;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
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
        addMove(row - 1, column , possibleMoves, myPosition, board, null); /* forward */
        addMove(row, column + 1, possibleMoves, myPosition, board, null); /* right */
        addMove(row, column -1, possibleMoves, myPosition, board, null); /* left */
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
        int row = myPosition.getRow(), column = myPosition.getColumn();
        while (inBoardRange(row, column)) {
            row += 1;
            column -= 1;
            addMove(row, column, possibleMoves, myPosition, board, null); /* backward left */
        }
        while (inBoardRange(row, column)) {
            row -= 1;
            column -= 1;
            addMove(row, column, possibleMoves, myPosition, board, null); /* forward left */
        }
        while (inBoardRange(row, column)) {
            row += 1;
            column += 1;
            addMove(row, column, possibleMoves, myPosition, board, null); /* backward right */
        }
        while (inBoardRange(row, column)) {
            row -= 1;
            column += 1;
            addMove(row, column, possibleMoves, myPosition, board, null); /* forward right */
        }
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
        int row = myPosition.getRow(), column = myPosition.getColumn();
        while (inBoardRange(row, column)) {
            column -= 1;
            addMove(row, column, possibleMoves, myPosition, board, null); /* left */
        }
        while (inBoardRange(row, column)) {
            column += 1;
            addMove(row, column, possibleMoves, myPosition, board, null); /* right */
        }
        while (inBoardRange(row, column)) {
            row += 1;
            addMove(row, column, possibleMoves, myPosition, board, null); /* backward */
        }
        while (inBoardRange(row, column)) {
            row -= 1;
            addMove(row, column, possibleMoves, myPosition, board, null); /* forward */
        }
        return possibleMoves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        int row = myPosition.getRow(), column = myPosition.getColumn();
        addMove(row - 1, column , possibleMoves, myPosition, board, PieceType.BISHOP); /* forward */
        addMove(row - 1, column , possibleMoves, myPosition, board, PieceType.QUEEN); /* forward */
        addMove(row - 1, column , possibleMoves, myPosition, board, PieceType.KNIGHT); /* forward */
        addMove(row - 1, column , possibleMoves, myPosition, board, PieceType.ROOK); /* forward */
        return possibleMoves;
    }

    private void addMove(int row, int column, Collection<ChessMove> moves, ChessPosition myPosition, ChessBoard board, ChessPiece.PieceType promotionPiece){
        ChessPosition newPosition = new ChessPosition(row, column);
        if((board.getPiece(newPosition) == null | board.getPiece(newPosition).color != this.color) && inBoardRange(newPosition)) {
            moves.add(new ChessMove(myPosition, newPosition, promotionPiece));
        }
    }

    private Boolean inBoardRange(int row, int column) {
        return row <= 8 && row > 0 && column <= 8 && column > 0;
    }

    private Boolean inBoardRange(ChessPosition position) {
        int row = position.getRow();
        int column = position.getColumn();
        return row <= 8 && row > 0 && column <= 8 && column > 0;
    }
}
