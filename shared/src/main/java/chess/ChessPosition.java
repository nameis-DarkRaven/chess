package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private int current_row;
    private int current_column;

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
