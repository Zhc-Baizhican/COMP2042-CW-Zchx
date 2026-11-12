package com.comp2042;

import javafx.scene.image.Image;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Coin {
    /** Cells spanned by the painted board, regardless of the playfield size. */
    private static final int BOARD_CELLS = 20;

    /** Where a coin is allowed to appear. */
    public enum Area {
        /** Anywhere in the playfield. */
        ANY,
        /** Only on the painted board. */
        BOARD,
        /** Only outside the painted board, in the transparent area. */
        OUTSIDE
    }

    private Point pos;
    private Image image = new Image("resources/views/res/redcoin.png");

    public Coin(int x, int y, Snake snek, Obstacle obstacle) {
        this(x, y, snek, obstacle, 0);
    }

    /**
     * @param spawnRadius when greater than zero, the coin is placed within this
     *                    many cells of the snake's head whenever possible, so
     *                    the player does not have to cross the whole board
     */
    public Coin(int x, int y, Snake snek, Obstacle obstacle, int spawnRadius) {
        this(x, y, snek, obstacle, spawnRadius, Area.ANY);
    }

    /**
     * @param area restricts the coin to the painted board, to the transparent
     *             area around it, or to the whole playfield
     */
    public Coin(int x, int y, Snake snek, Obstacle obstacle, int spawnRadius, Area area) {
        this.pos = pickFreeCell(x, y, snek, obstacle, new Random(), spawnRadius, area);
    }

    /**
     * Picks a random cell that is not occupied by the snake or by an obstacle.
     *
     * <p>The previous implementation retried random coordinates inside a
     * {@code do { ... } while (...)} loop with no upper bound, so once the board
     * was nearly full it could spin forever and freeze the game. Building the
     * list of free cells instead makes the cost predictable: one pass over the
     * grid, no retries.</p>
     *
     * @param width    number of columns in the board
     * @param height   number of rows in the board
     * @param snek     the snake, whose head and body must be avoided
     * @param obstacle the obstacle set, whose cells must be avoided
     * @param rnd      source of randomness
     * @return a random cell that is currently free
     * @throws IllegalStateException if no free cell is left
     */
    static Point pickFreeCell(int width, int height, Snake snek, Obstacle obstacle, Random rnd) {
        return pickFreeCell(width, height, snek, obstacle, rnd, 0);
    }

    /**
     * Picks a random free cell, optionally preferring cells close to the head.
     *
     * @param spawnRadius maximum distance from the head to prefer; {@code 0}
     *                    means any free cell is equally likely
     */
    static Point pickFreeCell(int width, int height, Snake snek, Obstacle obstacle, Random rnd, int spawnRadius) {
        return pickFreeCell(width, height, snek, obstacle, rnd, spawnRadius, Area.ANY);
    }

    /**
     * Picks a random free cell inside a region of the playfield.
     *
     * @param spawnRadius maximum distance from the head to prefer; {@code 0}
     *                    means any free cell in the region is equally likely
     * @param area        the region the coin must land in
     */
    static Point pickFreeCell(int width, int height, Snake snek, Obstacle obstacle, Random rnd,
                              int spawnRadius, Area area) {
        Set<Point> blocked = new HashSet<>(obstacle.getObstacles());
        blocked.addAll(snek.getBody());
        blocked.add(snek.getHead());

        int boardWidth = Math.min(width, BOARD_CELLS);
        int boardLeft = (width - boardWidth) / 2;
        int boardTop = (height - Math.min(height, BOARD_CELLS)) / 2;

        boolean wantBoard = area == Area.BOARD;
        boolean wantOutside = area == Area.OUTSIDE;

        Point head = snek.getHead();
        List<Point> free = new ArrayList<>();
        List<Point> nearHead = new ArrayList<>();
        for (int cx = 0; cx < width; cx++) {
            for (int cy = 0; cy < height; cy++) {
                Point candidate = new Point(cx, cy);
                if (blocked.contains(candidate)) {
                    continue;
                }
                boolean onBoard = cx >= boardLeft && cx < boardLeft + boardWidth
                        && cy >= boardTop && cy < boardTop + boardWidth;
                if (wantBoard && !onBoard) {
                    continue;
                }
                if (wantOutside && onBoard) {
                    continue;
                }
                free.add(candidate);
                if (spawnRadius > 0
                        && Math.abs(candidate.x - head.x) <= spawnRadius
                        && Math.abs(candidate.y - head.y) <= spawnRadius) {
                    nearHead.add(candidate);
                }
            }
        }

        if (free.isEmpty()) {
            // The requested region can be full while the rest of the playfield
            // is not, so fall back instead of ending the run.
            if (area != Area.ANY) {
                return pickFreeCell(width, height, snek, obstacle, rnd, spawnRadius, Area.ANY);
            }
            throw new IllegalStateException("No free cell left for a new coin");
        }
        // Fall back to the whole region when the head is boxed in.
        List<Point> candidates = nearHead.isEmpty() ? free : nearHead;
        return candidates.get(rnd.nextInt(candidates.size()));
    }

    public Point getPos() {
        return new Point(pos);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    public Image getImage() {
        return image;
    }
}
