package com.comp2042;

import javafx.scene.image.Image;

public class RainbowCoin extends Coin {
    private Image image = new Image("resources/views/res/rainbowcoin.png");

    public RainbowCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    public RainbowCoin(int x, int y, Snake snek, Obstacle obstacle, int spawnRadius) {
        super(x, y, snek, obstacle, spawnRadius);
    }

    public RainbowCoin(int x, int y, Snake snek, Obstacle obstacle, int spawnRadius, Coin.Area area) {
        super(x, y, snek, obstacle, spawnRadius, area);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    /** Extra animation rate this coin adds for the rest of the run. */
    public double getSpeedBonus() {
        return 5;
    }

    public Image getImage() {
        return image;
    }
}
