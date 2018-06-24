package gui.control;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class ControlAbout {

    // callback
    private Stage dialog;

    // initialization
    void initialSetup(Stage dialog) {
        this.dialog = dialog;
    }

    // events
    @FXML private void btnCloseClicked() {
        dialog.close();
    }

}
