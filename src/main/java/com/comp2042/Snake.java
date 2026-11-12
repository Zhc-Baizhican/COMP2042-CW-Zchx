package com.comp2042;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.util.ArrayList;

public class Snake {
    private boolean dead = false;
    private final Point head = new Point();
    private final ArrayList<Point> body = new ArrayList<>();
    private final int gx;
    private final int gy;
    private boolean ignoreBorders = false;
    private boolean noBorder;
    private boolean wallBlocked = false;
    private Obstacle obstacle;
    private Image imgInputHead = new Image("resources/views/res/snakehead.png");
    private ImageView imageHead = new ImageView(imgInputHead);
    private Image imgInputBody = new Image("resources/views/res/snakebody.png");
    private ImageView imageBody = new ImageView(imgInputBody);
    private Image imgInputTurn = new Image("resources/views/res/snaketurnleft.png");
    private ImageView imageTurn = new ImageView(imgInputTurn);
    private Image imgInputTail = new Image("resources/views/res/snaketail.png");
    private ImageView imageTail = new ImageView(imgInputTail);
    private int snakeDir = 270;

    public Snake(int x, int y, boolean noBorder, Obstacle obstacle) {
        this.obstacle = obstacle;
        this.noBorder = noBorder;
        this.head.setLocation(x / 2, y / 2);
        this.body.add(new Point(x / 2 + 1, y / 2));
        this.gx = x;
        this.gy = y;
        applySkin();
    }

    /**
     * Applies the colour preset chosen in the settings screen.
     *
     * <p>The snake is drawn from images, so the colour comes from a
     * {@code ColorAdjust} effect rather than from a colour field. All four
     * sprites share the same preset, otherwise the segments do not match.</p>
     */
    private void applySkin() {
        SnakeSkin skin = Settings.get().getSnakeSkin();
        imageHead.setEffect(skin.createEffect());
        imageBody.setEffect(skin.createEffect());
        imageTurn.setEffect(skin.createEffect());
        imageTail.setEffect(skin.createEffect());
    }

    public void move(char dir) {
        body.add(new Point(head));
        wallBlocked = false;
        switch (dir) {
            case 'U':
                snakeDir = 0;
                head.translate(0, -1);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(0, 1);
                    dead = true;
                } else if (borderControl() && ignoreBorders) {
                    head.setLocation(head.getX(), gy - 1);
                } else if (borderControl()) {
                    // Easy mode: the wall stops the snake for this step.
                    head.translate(0, 1);
                    wallBlocked = true;
                }
                break;

            case 'D':
                snakeDir = 180;
                head.translate(0, 1);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(0, -1);
                    dead = true;
                } else if (borderControl() && ignoreBorders) {
                    head.setLocation(head.getX(), 0);
                } else if (borderControl()) {
                    head.translate(0, -1);
                    wallBlocked = true;
                }
                break;

            case 'L':
                snakeDir = 270;
                head.translate(-1, 0);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(1, 0);
                    dead = true;
                } else if (borderControl() && ignoreBorders) {
                    head.setLocation(gx - 1, head.getY());
                } else if (borderControl()) {
                    head.translate(1, 0);
                    wallBlocked = true;
                }
                break;

            case 'R':
                snakeDir = 90;
                head.translate(1, 0);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(-1, 0);
                    dead = true;
                } else if (borderControl() && ignoreBorders) {
                    head.setLocation(0, head.getY());
                } else if (borderControl()) {
                    head.translate(-1, 0);
                    wallBlocked = true;
                }
                break;
        }

        if (body.size() > 4 && hitSelf() && !body.get(0).equals(head)) {
            dead = true;
        }
    }

    public int getBodyDir(int bodyCount) {
        if (bodyCount == 0) {
            return 0;
        } else if (body.get(bodyCount).getY() == body.get(bodyCount - 1).getY()) {
            return 270;
        } else {
            return 0;
        }
    }

    public boolean ateCoin(Coin coin) {
        if (!head.equals(coin.getPos())) {
            body.remove(0);
        }
        return head.equals(coin.getPos());
    }

    private boolean borderControl() {
        // The ghost-board hack removes the border rule entirely: the snake may
        // walk off the visible board and come back.
        if (Hacks.get().isEnabled(Hack.OFF_BOARD)) {
            return false;
        }
        return head.getX() < 0 || head.getY() < 0 || head.getX() >= gx || head.getY() >= gy;
    }

    public boolean hitSelf() {
        return body.contains(head);
    }

    public ArrayList<Point> getBody() {
        return new ArrayList<>(body);
    }

    public Point getHead() {
        return new Point(head);
    }

    public boolean getDead() {
        return dead;
    }

    /**
     * @return {@code true} when the last step was stopped by a wall instead of
     *         killing the snake, so the view can hint a turn
     */
    public boolean isWallBlocked() {
        return wallBlocked;
    }

    public void ignoreBorders(boolean ign) {
        ignoreBorders = ign;
    }

    public ImageView getImageViewHead() {
        return imageHead;
    }

    public ImageView getImageViewBody() {
        return imageBody;
    }

    public ImageView getImageViewTurn() {
        return imageTurn;
    }

    public int getSnakeDir() {
        return snakeDir;
    }

    public ImageView getImageViewTail() {
        return imageTail;
    }
}
