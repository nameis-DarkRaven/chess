package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private int current_row;
    private int current_column;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPosition that = (ChessPosition) o;
        return current_row == that.current_row && current_column == that.current_column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(current_row, current_column);
    }

    public ChessPosition(int row, int col) {
        this.current_row = row;
        this.current_column = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getCurrent_row() {return current_row;}

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return current_column;
    }
}
