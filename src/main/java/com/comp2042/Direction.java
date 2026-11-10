package com.comp2042;

/**
 * The four directions the snake can travel.
 *
 * <p>This replaces the raw {@code char} codes and the {@code KeyCode} constants
 * that used to be passed around. Once keys became rebindable, "which way is the
 * snake moving" had to stop depending on which physical key was pressed.</p>
 */
public enum Direction {

    UP('U', -1, 0),
    DOWN('D', 1, 0),
    LEFT('L', 0, -1),
    RIGHT('R', 0, 1);

    private final char moveChar;
    private final int rowDelta;
    private final int columnDelta;

    Direction(char moveChar, int rowDelta, int columnDelta) {
        this.moveChar = moveChar;
        this.rowDelta = rowDelta;
        this.columnDelta = columnDelta;
    }

    /**
     * @return the single-character code understood by {@link Snake#move(char)}
     */
    public char toMoveChar() {
        return moveChar;
    }

    /**
     * @param other another direction, may be {@code null}
     * @return {@code true} when the two directions point opposite ways, which
     *         the snake must not do because it would reverse into itself
     */
    public boolean isOpposite(Direction other) {
        return other != null && rowDelta == -other.rowDelta && columnDelta == -other.columnDelta;
    }
}
