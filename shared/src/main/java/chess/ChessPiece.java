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


    private enum Directions{
        UP,
        DOWN,
        LEFT,
        RIGHT,
        UPRIGHT,
        UPLEFT,
        DOWNRIGHT,
        DOWNLEFT
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();
        ChessPosition upRight = new ChessPosition(myPosition.getCurrent_row() + 1, myPosition.getColumn() + 1);
        if (board.getPiece(upRight) == null | board.getPiece(upRight).color != this.color) {
        possibleMoves.add(new ChessMove(myPosition, upRight, null));}

        ChessPosition downLeft = new ChessPosition(myPosition.getCurrent_row() - 1, myPosition.getColumn() - 1);
        if (board.getPiece(downLeft) == null | board.getPiece(upRight).color != this.color) {
        possibleMoves.add(new ChessMove(myPosition, downLeft, null));}

        ChessPosition upLeft = new ChessPosition(myPosition.getCurrent_row() - 1, myPosition.getColumn() + 1);
        if (board.getPiece(upLeft) == null | board.getPiece(upRight).color != this.color) {
        possibleMoves.add(new ChessMove(myPosition, upLeft, null));}

        ChessPosition downRight = new ChessPosition(myPosition.getCurrent_row() + 1, myPosition.getColumn() - 1);
        if (board.getPiece(downRight) == null | board.getPiece(upRight).color != this.color) {
        possibleMoves.add(new ChessMove(myPosition, downRight, null)); }

        ChessPosition left = new ChessPosition(myPosition.getCurrent_row(), myPosition.getColumn() - 1);
        if (board.getPiece(left) == null | board.getPiece(upRight).color != this.color) {
        possibleMoves.add(new ChessMove(myPosition, left, null)); }

        ChessPosition right = new ChessPosition(myPosition.getCurrent_row(), myPosition.getColumn() + 1);
        if (board.getPiece(right) == null | board.getPiece(upRight).color != this.color) {
        possibleMoves.add(new ChessMove(myPosition, right, null)); }

        ChessPosition up = new ChessPosition(myPosition.getCurrent_row() + 1, myPosition.getColumn());
        if (board.getPiece(up) == null | board.getPiece(upRight).color != this.color) {
        possibleMoves.add(new ChessMove(myPosition, up, null)); }

        ChessPosition down = new ChessPosition(myPosition.getCurrent_row() - 1, myPosition.getColumn());
        if (board.getPiece(down) == null | board.getPiece(upRight).color != this.color) {
        possibleMoves.add(new ChessMove(myPosition, down, null)); }

        return possibleMoves;
    }
    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();



        return possibleMoves;

    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();



        return possibleMoves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();



        return possibleMoves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();



        return possibleMoves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> possibleMoves = new ArrayList<>();



        return possibleMoves;

    }

    private Boolean inBoardRange (int row, int column, ChessBoard board, Directions direction){
        if (row <= 8 && row > 0 && column <= 8 && column > 0){
//            if (){
//
//            }
        }

        return false;
    }

}
