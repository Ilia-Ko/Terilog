package gui.control;

import javafx.stage.Stage;

public class ControlSettings {

    // callback
    private Stage dialog;
    private ControlMain control;

    void initialSetup(Stage dialog, ControlMain control) {
        this.dialog = dialog;
        this.control = control;
    }

}
