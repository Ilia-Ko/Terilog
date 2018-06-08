package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    // general dimensions
    private static final double GOLD = 0.5 * (1.0 + Math.sqrt(5.0));
    private static final double DEF_FONT_SIZE = 1.0 / 60.0;

    // dimensions
    private double maxWidth, maxHeight;
    private double defWidth, defHeight;
    private double minWidth, minHeight;
    private String defFont;

    @Override
    public void init() throws Exception {
        super.init();

        // take delicate care of DPI: (I hate when some apps look awful on Retina displays :)
        Rectangle2D rect = Screen.getPrimary().getBounds();
        maxWidth = rect.getWidth();
        maxHeight = rect.getHeight();

        // adjust font:
        int defFontSize = (int) Math.round(maxHeight * DEF_FONT_SIZE);
        defFont = "-fx-font: normal " + Integer.toString(defFontSize) + "px monospace";

        // set default dimensions
        minHeight = defFontSize * 5.0;
        minWidth = minHeight * GOLD;
        defWidth = maxWidth / GOLD;
        defHeight = maxHeight / GOLD;
    }

    @Override
    public void start(Stage stage) throws IOException {
        // adjust window geometry
        stage.setResizable(true);
        stage.setFullScreenExitHint("Press F11 to leave fullscreen mode.");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.setMaxWidth(maxWidth);
        stage.setMaxHeight(maxHeight);
        stage.setX((maxWidth - defWidth) * 0.5);
        stage.setY((maxHeight - defHeight) * 0.5);

        // setup scene and controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/main.fxml"));
        Parent root = loader.load();
        root.setStyle(defFont);
        Scene scene = new Scene(root, defWidth, defHeight);
        Control control = loader.getController();
        control.initialSetup(stage, defFont, maxWidth, maxHeight);

        // make window
        stage.setTitle("TERILOG");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }

}
