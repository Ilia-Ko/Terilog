package gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ControlSettings {

    // gui
    @FXML private TextField txtName;

    // callback
    private Stage dialog;
    private ControlMain control;

    // initialization
    void initialSetup(Stage dialog, ControlMain control) {
        this.dialog = dialog;
        this.control = control;
        txtName.setText(control.getCircuit().getNameProperty().get());
    }

    // events
    @FXML private void btnApplyClicked() {
        control.getCircuit().getNameProperty().setValue(txtName.getText());
        dialog.close();
    }
    @FXML private void btnCloseClicked() {
        dialog.close();
    }

}
