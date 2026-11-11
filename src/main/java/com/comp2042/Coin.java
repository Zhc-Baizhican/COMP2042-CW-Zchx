package com.comp2042;

import javafx.scene.image.Image;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Coin {
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
        this.pos = pickFreeCell(x, y, snek, obstacle, new Random(), spawnRadius);
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
        Set<Point> blocked = new HashSet<>(obstacle.getObstacles());
        blocked.addAll(snek.getBody());
        blocked.add(snek.getHead());

        Point head = snek.getHead();
        List<Point> free = new ArrayList<>();
        List<Point> nearHead = new ArrayList<>();
        for (int cx = 0; cx < width; cx++) {
            for (int cy = 0; cy < height; cy++) {
                Point candidate = new Point(cx, cy);
                if (blocked.contains(candidate)) {
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
            throw new IllegalStateException("No free cell left for a new coin");
        }
        // Fall back to the whole board when the head is boxed in.
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
