package com.comp2042;

import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Obstacle {
    private Color color = Color.BLACK;
    private ArrayList<Point> obstacles = new ArrayList<>();
    private int x;
    private int y;

    public Obstacle(int x, int y) {
       this.x = x;
       this.y = y;
    }

    public void newObstacles(Snake snek, Coin coin, int amount) {
        if (obstacles.size() > 50) {
            return;
        }
        Random rnd = new Random();
        Point p;
        for (int i=0; i<amount; i++) {
            do {
                p = new Point(rnd.nextInt(x) + 1, rnd.nextInt(y));
            } while (coin.getPos().equals(p) || snek.getBody().contains(p) || snek.getHead().equals(p));
            obstacles.add(p);
        }
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
