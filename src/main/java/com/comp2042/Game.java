package com.comp2042;

import javafx.scene.paint.Color;

public class Game {
    private static final double NORMAL_SPEED = 8.5;
    private static final double HARD_SPEED = 13;

    private Color color = Color.web("feffe4");
    private int width;
    private int height;
    private int score;
    private int maxScore;
    private double multiplier;
    private int scorePoints;
    private double speed;
    private boolean powerUp;
    private boolean noBorder;

    public Game(int x, int y, boolean powerUp, char difficulty) {
        // Every difficulty needs a value for both speed and noBorder. Previously
        // an unrecognised difficulty left speed at 0, which drove the animation
        // rate to zero and froze the game.
        switch (difficulty) {
            case 'E' -> {
                speed = NORMAL_SPEED;
                noBorder = true;
            }
            case 'H' -> {
                speed = HARD_SPEED;
                noBorder = false;
            }
            case 'N' -> {
                speed = NORMAL_SPEED;
                noBorder = false;
            }
            default -> {
                speed = NORMAL_SPEED;
                noBorder = false;
            }
        }

        this.powerUp = powerUp;
        width = x;
        height = y;
        // The snake starts with two cells (head plus one body segment) and grows
        // by exactly one cell per coin, so the board is full after this many coins.
        maxScore = x * y - 2;
        multiplier = height > 50 ? 10 : 20;
    }

    /**
     * The board is won when the snake occupies every cell.
     *
     * @return {@code true} once the snake can no longer grow
     */
    public boolean gameWon() {
        return score >= maxScore;
    }

    public void incScore() {
        score++;
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

    public void addPoints(int n) {
        scorePoints += n;
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
}
