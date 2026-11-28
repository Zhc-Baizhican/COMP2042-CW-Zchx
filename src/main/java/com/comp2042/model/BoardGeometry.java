package com.comp2042.model;

/**
 * Where the painted board sits inside the playfield.
 *
 * <p>The playfield can be larger than the board: the ghost-board hack turns the
 * whole screen into playfield while the board keeps its twenty cells. The same
 * rectangle was being recomputed in three classes (rendering, coin placement and
 * the "has the head left the board" test), which is exactly the kind of
 * duplication that drifts apart. It lives here once instead.</p>
 */
public final class BoardGeometry {

    /** Cells across the painted board, which is always square. */
    public static final int CELLS = 20;

    private BoardGeometry() {
    }

    /**
     * @param playfieldColumns columns in the playfield
     * @return how many of them the painted board spans
     */
    public static int columns(int playfieldColumns) {
        return Math.min(playfieldColumns, CELLS);
    }

    /**
     * @param playfieldRows rows in the playfield
     * @return how many of them the painted board spans
     */
    public static int rows(int playfieldRows) {
        return Math.min(playfieldRows, CELLS);
    }

    /**
     * @param playfieldColumns columns in the playfield
     * @return the first column of the painted board
     */
    public static int left(int playfieldColumns) {
        return (playfieldColumns - columns(playfieldColumns)) / 2;
    }

    /**
     * @param playfieldRows rows in the playfield
     * @return the first row of the painted board
     */
    public static int top(int playfieldRows) {
        return (playfieldRows - rows(playfieldRows)) / 2;
    }

    /**
     * @param x               column to test
     * @param y               row to test
     * @param playfieldColumns columns in the playfield
     * @param playfieldRows    rows in the playfield
     * @return {@code true} when the cell is on the painted board
     */
    public static boolean onBoard(int x, int y, int playfieldColumns, int playfieldRows) {
        int left = left(playfieldColumns);
        int top = top(playfieldRows);
        return x >= left && x < left + columns(playfieldColumns)
                && y >= top && y < top + rows(playfieldRows);
    }
}
