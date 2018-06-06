package gui;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

class Controller {

    // default grid
    private static final int NUM_VERTICAL_GRID_POINTS = 1000;
    private static final double GRID_POINT_THICKNESS = 1E-6;

    // colours
    private static final Color COL_CLEAR    = Color.rgb(192, 192, 192);
    private static final Color COL_GRID     = Color.rgb(0, 0, 0);

    // gui references
    private Main main;
    private Stage stage;
    private Canvas field;

    // general logic
    private double w, h;
    private boolean isFullscreen;

    Controller(Main main, Stage stage) {
        // copy gui references
        this.main = main;
        this.stage = stage;

        // init general logic
        isFullscreen = false;
    }
    void setField(Canvas field) {
        this.field = field;
        w = field.getWidth();
        h = field.getHeight();
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
        main.measureGUI();
        main.resizeGUI();
    }
    void onMenuExitClicked(Event event) {
        Platform.exit();
    }
    // Menu.About
    void onMenuHelpClicked(Event event) {

    }
    void onMenuCreditsClicked(Event event) {

    }

    // Tools
    void onToolHardNClicked(Event event) {
        Main.sout("Hard N clicked.\n");
    }
    void onToolHardPClicked(Event event) {

    }
    void onToolSoftNClicked(Event event) {

    }
    void onToolSoftPClicked(Event event) {

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
        int numHorizontalGridPoints = Main.rnd(w / h * NUM_VERTICAL_GRID_POINTS);
        double d = h / NUM_VERTICAL_GRID_POINTS;
        double r = h * GRID_POINT_THICKNESS;
        gc.setFill(COL_GRID);
        gc.setLineWidth(r);
        for (int i = 0; i < NUM_VERTICAL_GRID_POINTS; i++)
            for (int j = 0; j < numHorizontalGridPoints; j++)
                gc.fillOval(i * d, j * d, r, r);
    }

}
