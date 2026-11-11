package com.comp2042;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URL;

public class GameView {

    public Canvas gridCanvas;
    public GraphicsContext gc;

    public Label scoreLabel;
    public StackPane statusOverlay;
    private Label statusTitle;
    private Label statusDetail;

    private Game game;
    private double initX;
    private double initY;
    private double fullScale = 1;
    private boolean fullsize = false;
    private int[][] turnArray = new int[MainMenuController.getxValue()][MainMenuController.getyValue()];

    public GameView(Game game) {
        this.game = game;
    }

    public void makeScene(Scene scene, Snake snek, Coin coin) {
        Scene gameScene = scene;
        gridCanvas = new Canvas(gameScene.getWidth(), gameScene.getHeight());
        initX = scene.getWidth();
        initY = scene.getHeight();
        gc = gridCanvas.getGraphicsContext2D();
        drawGrid(coin, snek, gc);

        addStylesheet(scene);

        scoreLabel = new Label("Score: " + game.getScorePoints());
        scoreLabel.setId("scoreLabel");

        statusTitle = new Label();
        statusTitle.getStyleClass().add("statusTitle");
        statusDetail = new Label();
        statusDetail.getStyleClass().add("statusDetail");
        statusDetail.setWrapText(true);
        statusDetail.setTextAlignment(TextAlignment.CENTER);

        VBox statusBox = new VBox(statusTitle, statusDetail);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setSpacing(12);

        statusOverlay = new StackPane(statusBox);
        statusOverlay.getStyleClass().add("statusOverlay");
        statusOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        statusOverlay.setVisible(false);
    }

    /** Attaches the stylesheet once, at the scene level, instead of per label. */
    private void addStylesheet(Scene scene) {
        URL css = getClass().getResource("/resources/views/styling.css");
        if (css == null) {
            System.err.println("Stylesheet not found: /resources/views/styling.css");
            return;
        }
        scene.getStylesheets().add(css.toExternalForm());
    }

    public void scaleToFullscreen(boolean fullScreen, Stage primaryStage, Obstacle obstacle, Snake snek, Coin coin) {
        primaryStage.setFullScreen(fullScreen);

        // Scale against the matching axis: width against width, height against
        // height. The original code paired width with height, so the board could
        // overflow the screen on wide monitors.
        double widthScale = Screen.getPrimary().getVisualBounds().getWidth() / initX;
        double heightScale = Screen.getPrimary().getVisualBounds().getHeight() / initY;

        if (fullScreen && !fullsize) {
            fullScale = Math.min(widthScale, heightScale);
            gridCanvas.setWidth(gridCanvas.getWidth() * fullScale);
            gridCanvas.setHeight(gridCanvas.getHeight() * fullScale);
            fullsize = true;
        } else if (!fullScreen) {
            fullScale = 1;
            gridCanvas.setWidth(initX);
            gridCanvas.setHeight(initY);
            fullsize = false;
        }

        drawGrid(coin, snek, obstacle, gc, false, 0);
    }

    public void drawGrid(Coin coin, Snake snek, Obstacle obstacle, GraphicsContext gc, boolean a, int b) {
        gc.setFill(game.getColor());
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        gc.drawImage(coin.getImage(), (coin.getPos().x * game.getMultiplier()) * fullScale, (coin.getPos().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale - 3 * fullScale, game.getMultiplier() * fullScale - 3 * fullScale);

        int bodyIndex = 0;
        for (Point point : snek.getBody()) {
            drawBody(snek, point, bodyIndex);
            bodyIndex++;
        }

        drawHead(snek);

        gc.setFill(obstacle.getColor());
        for (Point point : obstacle.getObstacles()) {
            gc.fillRect(point.x * game.getMultiplier() * fullScale, point.y * game.getMultiplier() * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
        }
    }

    private void drawGrid(Coin coin, Snake snek, GraphicsContext gc) {
        gc.setFill(game.getColor());
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        gc.drawImage(coin.getImage(), (coin.getPos().x * game.getMultiplier()) * fullScale, (coin.getPos().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale - 3 * fullScale, game.getMultiplier() * fullScale - 3 * fullScale);

        int bodyIndex = 0;
        for (Point point : snek.getBody()) {
            drawBody(snek, point, bodyIndex);
            bodyIndex++;
        }

        drawHead(snek);
    }

    private void drawHead(Snake snek) {
        snek.getImageViewHead().setRotate(snek.getSnakeDir());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image rotatedImage = snek.getImageViewHead().snapshot(params, null);
        gc.drawImage(rotatedImage, (snek.getHead().x * game.getMultiplier()) * fullScale, (snek.getHead().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
    }

    private void drawBody(Snake snek, Point point, int index) {
        Image rotatedImage;
        if (snek.getBody().size() <= 1) {
            snek.getImageViewTail().setRotate(snek.getSnakeDir());
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewTail().snapshot(params, null);
        } else if (index == 0) {
            int rotateTail;
            if (snek.getBody().get(1).getX() == snek.getBody().get(0).getX() && snek.getBody().get(1).getY() == snek.getBody().get(0).getY() - 1) {
                rotateTail = 0;
            } else if (snek.getBody().get(1).getX() == snek.getBody().get(0).getX() && snek.getBody().get(1).getY() == snek.getBody().get(0).getY() + 1) {
                rotateTail = 180;
            } else if (snek.getBody().get(1).getX() == snek.getBody().get(0).getX() - 1 && snek.getBody().get(1).getY() == snek.getBody().get(0).getY()) {
                rotateTail = 270;
            } else {
                rotateTail = 90;
            }
            snek.getImageViewTail().setRotate(rotateTail);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewTail().snapshot(params, null);
        } else if (index > 0 && (turnArray[(int) snek.getBody().get(index).getX()][(int) snek.getBody().get(index).getY()] != 0)) {
            snek.getImageViewTurn().setRotate(90 * turnArray[(int) snek.getBody().get(index).getX()][(int) snek.getBody().get(index).getY()]);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewTurn().snapshot(params, null);
        } else {
            snek.getImageViewBody().setRotate(snek.getBodyDir(index));
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewBody().snapshot(params, null);
        }

        if (turnArray[(int) snek.getBody().get(0).getX()][(int) snek.getBody().get(0).getY()] != 0) {
            turnArray[(int) snek.getBody().get(0).getX()][(int) snek.getBody().get(0).getY()] = 0;
        }

        gc.drawImage(rotatedImage, (point.x * game.getMultiplier()) * fullScale, (point.y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
    }

    /**
     * Records how the snake turned at its current head cell, so the correct
     * corner sprite is drawn for that segment.
     *
     * @param snek the snake
     * @param from the direction it was travelling before the turn
     * @param to   the direction it now travels
     */
    public void turnDir(Snake snek, Direction from, Direction to) {
        int value = 0;
        if ((from == Direction.LEFT && to == Direction.UP) || (from == Direction.DOWN && to == Direction.RIGHT)) {
            value = 2;
        } else if ((from == Direction.LEFT && to == Direction.DOWN) || (from == Direction.UP && to == Direction.RIGHT)) {
            value = 3;
        } else if ((from == Direction.RIGHT && to == Direction.UP) || (from == Direction.DOWN && to == Direction.LEFT)) {
            value = 1;
        } else if ((from == Direction.UP && to == Direction.LEFT) || (from == Direction.RIGHT && to == Direction.DOWN)) {
            value = 4;
        }
        turnArray[(int) snek.getHead().getX()][(int) snek.getHead().getY()] = value;
    }

    /**
     * Shows the single status overlay for whichever state currently applies.
     * Win and loss take priority over pause, because the snake is not moving in
     * either of those states anyway.
     *
     * @param snek   the snake, used to detect a lost game
     * @param paused whether the player has paused the game
     */
    public void updateStatus(Snake snek, boolean paused) {
        String newline = System.lineSeparator();
        if (game.gameWon()) {
            showStatus("YOU WON!", "Score: " + game.getScorePoints() + newline
                    + "Press " + keyLabel(Action.RESTART) + " to play again");
        } else if (snek.getDead()) {
            showStatus("GAME OVER", "Score: " + game.getScorePoints() + newline
                    + keyLabel(Action.RESTART) + " restart        " + keyLabel(Action.BACK_TO_MENU) + " menu");
        } else if (paused) {
            showStatus("PAUSED", "Press " + keyLabel(Action.PAUSE) + " to resume");
        } else {
            hideStatus();
        }
    }

    /** Key name for the on-screen hints, so they follow a rebind. */
    private String keyLabel(Action action) {
        return KeyBindings.get().labelFor(action);
    }

    private void showStatus(String title, String detail) {
        statusTitle.setText(title);
        statusDetail.setText(detail);
        statusOverlay.setVisible(true);
    }

    private void hideStatus() {
        statusOverlay.setVisible(false);
    }

    public void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getScorePoints() + " / " + game.getTargetScore());
    }

}
