package com.comp2042;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.nio.file.Path;

/**
 * Controller for the run history.
 *
 * <p>Each row shows what the run looked like when it ended, the mode, the
 * score, the snake length and the hacks that were on.</p>
 */
public class HistoryController {

    private static final double THUMBNAIL = 96.0;

    @FXML
    private VBox entryList;

    @FXML
    private Label emptyLabel;

    private final ViewManager viewManager = Main.getViewManager();

    @FXML
    private void initialize() {
        refresh();
        installEscape();
    }

    private void refresh() {
        entryList.getChildren().clear();
        var records = RunHistory.get().getRecords();
        emptyLabel.setVisible(records.isEmpty());

        for (RunRecord record : records) {
            entryList.getChildren().add(buildRow(record));
        }
    }

    private HBox buildRow(RunRecord record) {
        HBox row = new HBox(10);
        row.getStyleClass().add("historyRow");
        row.setMaxWidth(Double.MAX_VALUE);

        row.getChildren().add(buildThumbnail(record));

        VBox details = new VBox(2);
        details.getStyleClass().add("historyDetails");

        Label headline = new Label(record.getResult() + "  ·  " + record.getMode());
        headline.getStyleClass().add("historyHeadline");
        details.getChildren().add(headline);

        details.getChildren().add(line("Score " + record.getScore()
                + "      Time " + record.getDuration()
                + "      Length " + record.getLength()));
        details.getChildren().add(line(record.getTimestamp()));

        if (record.hadHacks()) {
            details.getChildren().add(line("Hacks: " + record.getHacks()));
        } else {
            details.getChildren().add(line("Hacks: none"));
        }

        row.getChildren().add(details);
        return row;
    }

    private Label line(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("historyLine");
        label.setWrapText(true);
        return label;
    }

    private ImageView buildThumbnail(RunRecord record) {
        ImageView view = new ImageView();
        view.setFitWidth(THUMBNAIL);
        view.setFitHeight(THUMBNAIL);
        view.setPreserveRatio(true);
        view.getStyleClass().add("historyShot");

        if (record.getImageName() != null && !record.getImageName().isBlank()) {
            Path path = RunHistory.get().getShotsDirectory().resolve(record.getImageName());
            File file = path.toFile();
            if (file.exists()) {
                view.setImage(new Image(file.toURI().toString(), THUMBNAIL, THUMBNAIL, true, true, true));
            }
        }
        return view;
    }

    @FXML
    private void handleClear() {
        RunHistory.get().clear();
        refresh();
    }

    @FXML
    private void handleBack() {
        viewManager.showMenu();
    }

    /** Escape leaves the history, like the Back button. */
    private void installEscape() {
        Platform.runLater(() -> {
            if (entryList.getScene() != null) {
                entryList.getScene().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        handleBack();
                    }
                });
            }
        });
    }
}
