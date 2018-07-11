package gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ControlSettings {

    // gui
    @FXML private TextField txtName;
    @FXML private TextField txtDepth;
    @FXML private TextField txtFreq;

    // callback
    private Stage dialog;
    private ControlMain control;

    // initialization
    void initialSetup(Stage dialog, ControlMain control) {
        this.dialog = dialog;
        this.control = control;
        txtName.setText(control.getCircuit().nameProperty().get());
        txtDepth.setText(Integer.toString(control.getCircuit().simDepthProperty().get()));
    }

    // events
    @FXML private void txtKeyPressed(KeyEvent key) {
        KeyCode code = key.getCode();
        if (code.isLetterKey() || code.isWhitespaceKey()) {
            String text = txtDepth.getText();
            text = text.substring(0, text.length() - 1);
            txtDepth.setText(text);
        }
    }
    @FXML private void btnApplyClicked() {
        control.getCircuit().nameProperty().setValue(txtName.getText());
        control.getCircuit().simDepthProperty().setValue(Integer.parseInt(txtDepth.getText()));
        control.getCircuit().simFrequencyProperty().setValue(Double.parseDouble(txtFreq.getText()));
        dialog.close();
    }
    @FXML private void btnCloseClicked() {
        dialog.close();
    }

}
