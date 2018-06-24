package gui.control;

import javafx.fxml.FXML;
import javafx.stage.Stage;

public class ControlCredits {

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
