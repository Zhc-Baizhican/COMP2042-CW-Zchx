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
        this.pos = pickFreeCell(x, y, snek, obstacle, new Random());
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
        Set<Point> blocked = new HashSet<>(obstacle.getObstacles());
        blocked.addAll(snek.getBody());
        blocked.add(snek.getHead());

        List<Point> free = new ArrayList<>();
        for (int cx = 0; cx < width; cx++) {
            for (int cy = 0; cy < height; cy++) {
                Point candidate = new Point(cx, cy);
                if (!blocked.contains(candidate)) {
                    free.add(candidate);
                }
            }
        }

        if (free.isEmpty()) {
            throw new IllegalStateException("No free cell left for a new coin");
        }
        return free.get(rnd.nextInt(free.size()));
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
