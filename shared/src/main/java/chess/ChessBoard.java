package chess;

import java.util.Arrays;
import java.util.Objects;


public class ChessBoard {
    private ChessPiece[][] pieces;

    public ChessBoard() {
        pieces = new ChessPiece[8][8];
    }

    /** Adds a chess piece to the chessboard. */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        pieces[8 - position.getRow()][position.getColumn() - 1] = piece;
    }

    public ChessPiece[][] getPieces() {
        return pieces;
    }

    /** Gets a chess piece on the chessboard. */
    public ChessPiece getPiece(ChessPosition position) {
        return pieces[8 - position.getRow()][position.getColumn() - 1];
    }

    /** Sets the board to the default starting board. */
    public void resetBoard() {
        pieces = new ChessPiece[8][8];
        setBoard(ChessGame.TeamColor.WHITE, 1, 2);
        setBoard(ChessGame.TeamColor.BLACK, 8, 7);
    }

    /** Sets the board to the default starting board for a given color.
     *  Exists to limit duplicate code. */
    private void setBoard(ChessGame.TeamColor color, int side, int pawnLayer) {
        addPiece(new ChessPosition(side, 1), new ChessPiece(color, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(side, 2), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(side, 3), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(side, 4), new ChessPiece(color, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(side, 5), new ChessPiece(color, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(side, 6), new ChessPiece(color, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(side, 7), new ChessPiece(color, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(side, 8), new ChessPiece(color, ChessPiece.PieceType.ROOK));

        for (int column = 1; column < pieces.length + 1; column++) {
            addPiece(new ChessPosition(pawnLayer, column), new ChessPiece(color, ChessPiece.PieceType.PAWN));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(pieces, that.pieces);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(pieces);
    }

    @Override
    public String toString() {
        return "ChessBoard{" +
                "pieces=" + Arrays.deepToString(pieces) +
                '}';
    }
}
