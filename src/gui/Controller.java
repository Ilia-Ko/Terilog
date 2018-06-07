package gui;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

class Controller {

    // default grid
    private static final double DEF_GRID_PERIOD = 0.002;
    private static final double GRID_POINT_THICKNESS = DEF_GRID_PERIOD * 0.01;

    // colours
    private static final Color COL_CLEAR    = Color.rgb(192, 192, 192);
    private static final Color COL_GRID     = Color.rgb(0, 0, 0);

    private Stage stage;
    private Canvas field;

    // general logic
    private double w, h;    // field width and height
    private double p;       // field grid period
    private boolean isFullscreen;

    Controller(Stage stage) {
        // copy gui references
        this.stage = stage;

        // init general logic
        isFullscreen = false;
    }
    void setField(Canvas field) {
        this.field = field;
        w = field.getWidth();
        h = field.getHeight();
        p = w * DEF_GRID_PERIOD;
    }

    // Menu.File
    void onMenuOpenClicked(Event event) {

    }
    void onMenuSaveClicked(Event event) {

    }
    void onMenuSaveAsClicked(Event event) {

    }
    void onMenuToggleFullscreenClicked(Event event) {
        isFullscreen = !isFullscreen;
        stage.hide();
        stage.setFullScreen(isFullscreen);
    }
    void onMenuExitClicked(Event event) {
        Platform.exit();
    }
    // Menu.Add
    void onMenuHardNClicked(Event event) {
        Main.sout("Hard N clicked.\n");
    }
    void onMenuHardPClicked(Event event) {

    }
    void onMenuSoftNClicked(Event event) {

    }
    void onMenuSoftPClicked(Event event) {

    }
    void onMenuWireClicked(Event event) {

    }
    void onMenuVoltageClicked(Event event) {

    }
    // Menu.About
    void onMenuHelpClicked(Event event) {

    }
    void onMenuCreditsClicked(Event event) {

    }

    // Field
    void onFieldClicked(Event event) {

    }
    void clearField() {
        GraphicsContext gc = field.getGraphicsContext2D();

        // clear background
        gc.setFill(COL_CLEAR);
        gc.fillRect(0.0, 0.0, w, h);

        // draw grid points
        int numHorizontalGridPoints = Main.rnd(w / p);
        int numVerticalGridPoints = Main.rnd(h / p);
        double r = p * GRID_POINT_THICKNESS;
        gc.setFill(COL_GRID);
        gc.setLineWidth(r);
        for (int i = 0; i < numHorizontalGridPoints; i++)
            for (int j = 0; j < numVerticalGridPoints; j++)
                gc.fillOval(i * p, j * p, r, r);
    }

}
