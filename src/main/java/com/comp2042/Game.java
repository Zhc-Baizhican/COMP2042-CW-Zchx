package com.comp2042;

import javafx.scene.paint.Color;

public class Game {
    private static final double EASY_SPEED = 5.0;
    private static final double NORMAL_SPEED = 8.5;
    private static final double HARD_SPEED = 13;

    /**
     * Points needed to win, one target per difficulty. Easy asks for less so a
     * run can actually be finished instead of only ending in a collision.
     */
    private static final int EASY_TARGET = 100;
    private static final int NORMAL_TARGET = 150;
    private static final int HARD_TARGET = 200;

    private static final Color DEFAULT_BACKGROUND = Color.web("feffe4");
    private static final Color DEFAULT_NIGHT_BACKGROUND = Color.web("1b1f27");

    /** Cell size used by the convenience constructor, matching the old board. */
    private static final double DEFAULT_CELL_SIZE = 20.0;

    private Color color;
    private int width;
    private int height;
    private int score;
    private int maxScore;
    private double multiplier;
    private int scorePoints;
    private double speed;
    private boolean powerUp;
    private boolean noBorder;
    private int targetScore;
    private final char difficulty;

    /**
     * Convenience constructor for callers that do not care about pixel sizes,
     * mainly tests. The application always supplies a cell size so the board
     * fits the window.
     *
     * @param x          number of columns
     * @param y          number of rows
     * @param powerUp    whether special coins can spawn
     * @param difficulty one of {@code 'E'}, {@code 'N'}, {@code 'H'}
     */
    public Game(int x, int y, boolean powerUp, char difficulty) {
        this(x, y, powerUp, difficulty, DEFAULT_CELL_SIZE);
    }

    /**
     * @param x          number of columns
     * @param y          number of rows
     * @param powerUp    whether special coins can spawn
     * @param difficulty one of {@code 'E'}, {@code 'N'}, {@code 'H'}
     * @param cellSize   pixels per cell, chosen so the board fits the window
     */
    public Game(int x, int y, boolean powerUp, char difficulty, double cellSize) {
        this.difficulty = difficulty;
        // Every difficulty needs a value for both speed and noBorder. Previously
        // an unrecognised difficulty left speed at 0, which drove the animation
        // rate to zero and froze the game.
        switch (difficulty) {
            case 'E' -> {
                speed = EASY_SPEED;
                noBorder = true;
                targetScore = EASY_TARGET;
            }
            case 'H' -> {
                speed = HARD_SPEED;
                noBorder = false;
                targetScore = HARD_TARGET;
            }
            case 'N' -> {
                speed = NORMAL_SPEED;
                noBorder = false;
                targetScore = NORMAL_TARGET;
            }
            default -> {
                speed = NORMAL_SPEED;
                noBorder = false;
                targetScore = NORMAL_TARGET;
            }
        }

        this.powerUp = powerUp;
        width = x;
        height = y;
        // The snake starts with two cells (head plus one body segment) and grows
        // by exactly one cell per coin, so the board is full after this many coins.
        maxScore = x * y - 2;

        Settings settings = Settings.get();
        multiplier = cellSize;
        color = settings.isDarkTheme() ? DEFAULT_NIGHT_BACKGROUND : DEFAULT_BACKGROUND;
    }

    /**
     * A run is won by reaching the target score for the chosen difficulty.
     * Filling the whole board also counts, which keeps the original rule as a
     * fallback.
     *
     * @return {@code true} once the target score is reached
     */
    public boolean gameWon() {
        return scorePoints >= targetScore || score >= maxScore;
    }

    /**
     * @return the number of points needed to win on this difficulty
     */
    public int getTargetScore() {
        return targetScore;
    }

    public void incScore() {
        score++;
    }

    /**
     * Marks the run as won regardless of the score, for the "Win" hack.
     */
    public void winNow() {
        scorePoints = Math.max(targetScore, scorePoints);
        score = Math.max(maxScore, score);
    }

    /**
     * Reduces the number of coins needed to fill the board, because a cell
     * occupied by an obstacle can never be filled by the snake.
     *
     * @param cells number of cells that became unusable; subtracted as given
     */
    public void decMaxScore(int cells) {
        this.maxScore -= cells;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void resetScore() {
        score = 0;
        scorePoints = 0;
    }

    public Color getColor() {
        return color;
    }

    public double getMultiplier() {
        return multiplier;
    }

    /**
     * Adds points, never dropping below zero so the respawn penalty cannot
     * produce a negative score.
     *
     * @param n points to add, may be negative
     */
    public void addPoints(int n) {
        scorePoints = Math.max(0, scorePoints + n);
    }

    public int getScorePoints() {
        return scorePoints;
    }

    public boolean isPowerUp() {
        return powerUp;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isNoBorder() {
        return noBorder;
    }

    /**
     * Hard mode is the only difficulty that spawns obstacles during play, so
     * the silver coin that adds them is reserved for it.
     *
     * @return {@code true} when the game was started on the hard preset
     */
    public boolean isHardMode() {
        return difficulty == 'H';
    }

    /**
     * @return {@code true} for the easy preset, which is the only mode that
     *         spawns coins near the head and grants a second chance
     */
    public boolean isEasyMode() {
        return difficulty == 'E';
    }
}
