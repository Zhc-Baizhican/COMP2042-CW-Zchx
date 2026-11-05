package com.comp2042;

import javafx.scene.image.Image;

public class BlueCoin extends Coin {
    private Image image = new Image("resources/views/res/bluecoin.png");

    public BlueCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    public Image getImage() {
        return image;
    }
}
