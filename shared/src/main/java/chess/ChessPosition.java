package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private int currentRow;
    private int currentColumn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPosition that = (ChessPosition) o;
        return currentRow == that.currentRow && currentColumn == that.currentColumn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentRow, currentColumn);
    }

    public ChessPosition(int row, int col) {
        this.currentRow = row;
        this.currentColumn = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {return currentRow;}

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return currentColumn;
    }
}
