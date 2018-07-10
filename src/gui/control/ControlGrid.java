package gui.control;

import gui.Main;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControlGrid {

    // input bounds
    private static final int MIN_GRID_SIZE = 12;
    private static final int MAX_GRID_SIZE = 144;

    // gui
    @FXML private TextField txtWidth;
    @FXML private TextField txtHeight;

    // callback
    private Stage dialog;
    private IntegerProperty width, height;

    // initialization
    void initialSetup(Stage dialog, ControlMain control) {
        this.dialog = dialog;
        width = control.getGridWidth();
        height = control.getGridHeight();
        txtWidth.setText(Integer.toString(width.get()));
        txtHeight.setText(Integer.toString(height.get()));
    }

    // events
    @FXML private void btnApplyClicked() {
        try {
            // get width and height
            int w = Integer.parseInt(txtWidth.getText());
            int h = Integer.parseInt(txtHeight.getText());

            // check bounds
            if (w <= MAX_GRID_SIZE && w >= MIN_GRID_SIZE && h <= MAX_GRID_SIZE && h >= MIN_GRID_SIZE) {
                width.setValue(w);
                height.setValue(h);
                dialog.close();
            } else {
                // report input bounds
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(Main.TITLE);
                alert.setHeaderText("Input bounds:");
                alert.setContentText(String.format("Grid width and height are between %d and %d.", MIN_GRID_SIZE, MAX_GRID_SIZE));
                alert.showAndWait();
            }
        } catch (NumberFormatException e) {
            // report malformed input
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(Main.TITLE);
            alert.setHeaderText("Malformed input:");
            alert.setContentText("Grid width and height must be integers.");
            alert.showAndWait();
        }
    }
    @FXML private void btnCloseClicked() {
        dialog.close();
    }

}
