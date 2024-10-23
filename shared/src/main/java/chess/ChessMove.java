package chess;

import java.util.Objects;


public class ChessMove {
    private ChessPosition start;
    private ChessPosition end;
    private ChessPiece.PieceType type;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.start = startPosition;
        this.end = endPosition;
        this.type = promotionPiece;
    }


    public ChessPosition getStartPosition() {
        return start;
    }


    public ChessPosition getEndPosition() {
        return end;
    }


    public ChessPiece.PieceType getPromotionPiece() { return type; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(start, chessMove.start) && Objects.equals(end, chessMove.end) && type == chessMove.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, type);
    }

    @Override
    public String toString() {
        return "ChessMove{" +
                "start=" + start +
                ", end=" + end +
                ", type=" + type +
                '}';
    }
}
