package com.comp2042;

import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Obstacle {
    private static final int MAX_OBSTACLES = 50;

    private static final Color DAY_COLOR = Color.BLACK;
    /** A black obstacle would be invisible on the night background. */
    private static final Color NIGHT_COLOR = Color.web("7f8a9e");

    private Color color;
    private ArrayList<Point> obstacles = new ArrayList<>();
    private int x;
    private int y;

    public Obstacle(int x, int y) {
       this.x = x;
       this.y = y;
       this.color = Settings.get().isDarkTheme() ? NIGHT_COLOR : DAY_COLOR;
    }

    /**
     * Adds up to {@code amount} new obstacles on cells that are still free.
     *
     * <p>Two problems in the previous version are fixed here:</p>
     * <ul>
     *   <li>{@code rnd.nextInt(x) + 1} produced columns in {@code [1, x]}, so an
     *       obstacle could land on column {@code x}, one cell outside the board.
     *       Columns are now {@code [0, x - 1]}.</li>
     *   <li>The unbounded retry loop could spin forever when few cells were
     *       free. Free cells are now collected first and shuffled, so the method
     *       always terminates and never overlaps an existing obstacle.</li>
     * </ul>
     *
     * @param snek   the snake, whose head and body must be avoided
     * @param coin   the current coin, whose cell must be avoided
     * @param amount requested number of new obstacles; capped by free cells
     */
    public void newObstacles(Snake snek, Coin coin, int amount) {
        if (obstacles.size() >= MAX_OBSTACLES || amount <= 0) {
            return;
        }

        Set<Point> blocked = new HashSet<>(obstacles);
        blocked.addAll(snek.getBody());
        blocked.add(snek.getHead());
        blocked.add(coin.getPos());

        List<Point> free = new ArrayList<>();
        for (int cx = 0; cx < x; cx++) {
            for (int cy = 0; cy < y; cy++) {
                Point candidate = new Point(cx, cy);
                if (!blocked.contains(candidate)) {
                    free.add(candidate);
                }
            }
        }

        Collections.shuffle(free, new Random());
        int room = MAX_OBSTACLES - obstacles.size();
        int toAdd = Math.min(Math.min(amount, free.size()), room);
        obstacles.addAll(free.subList(0, toAdd));
    }

    public Color getColor() {
        return color;
    }

    public ArrayList<Point> getObstacles() {
        return obstacles;
    }

    public void reset() {
        obstacles.clear();
    }
}
