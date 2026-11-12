package com.comp2042;

import javafx.scene.image.Image;

public class SilverCoin extends Coin {
    private Image image = new Image("resources/views/res/silvercoin.png");

    public SilverCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    public SilverCoin(int x, int y, Snake snek, Obstacle obstacle, int spawnRadius) {
        super(x, y, snek, obstacle, spawnRadius);
    }

    public SilverCoin(int x, int y, Snake snek, Obstacle obstacle, int spawnRadius, Coin.Area area) {
        super(x, y, snek, obstacle, spawnRadius, area);
    }

    public Image getImage() {
        return image;
    }

    public int getPoints() {
        int points = 5;
        return points;
    }
}
