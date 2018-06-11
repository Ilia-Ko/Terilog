package gui;

import engine.Circuit;
import engine.Component;
import engine.Wire;
import engine.interfaces.Informative;
import engine.transistors.HardN;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.Robot;
import java.awt.AWTException;

public class Control {

    // dimensions
    private static final double GRID_PERIOD = 0.01; // relative to screen width
    private static final double GRID_POINT_RADIUS = 1.0 / 12.0; // in periods
    public static final double LINE_WIDTH = 1.0 / 10.0; // in periods
    // input bounds
    private static final int MIN_GRID_SIZE = 12;
    private static final int MAX_GRID_SIZE = 20736;
    private static final double MIN_GRID_PERIOD = 1.0 / 6.0; // relative to period
    private static final double MAX_GRID_PERIOD = 1.0 * 144.0; // relative to period
    // colours
    private static final Color COL_CLEAR = Color.rgb(219, 255, 244);
    private static final Color COL_GRID  = Color.rgb(0, 0, 0);

    // some gui
    private Stage stage;
    @FXML private MenuItem menuPlay;
    @FXML private MenuItem menuZoomIn;
    @FXML private MenuItem menuZoomOut;
    @FXML private Canvas field; // field for circuits
    @FXML private Canvas fly; // flying canvas for object insertion
    private String defFont;

    // dimensions
    private double p; // the period of the field grid in pixels
    private double minPeriod, maxPeriod; // in pixels
    private int w, h; // field width and height in periods

    // circuit logic
    private Circuit circuit;

    // object insertion logic
    private boolean holdingWire, holdingComp;
    private Wire flyWire;
    private Component flyComp;
    private Robot robot;

    // initialization
    void initialSetup(Stage stage, String defFont, double screenWidth, double screenHeight) {
        this.stage = stage;
        this.defFont = defFont;

        // dimensions
        p = screenWidth * GRID_PERIOD;
        w = rnd(screenWidth / p);
        h = rnd(screenHeight / p);
        minPeriod = p * MIN_GRID_PERIOD; if (minPeriod < 1.0) minPeriod = 1.0;
        maxPeriod = p * MAX_GRID_PERIOD;

        // circuit logic
        circuit = new Circuit();

        // init insertion logic
        holdingWire = false;
        holdingComp = false;
        try { robot = new Robot(); } catch (AWTException e) { e.printStackTrace(); }

        // prepare field
        updateGridParameters();
        renderField();
    }

    // circuit field actions
    @FXML private void fieldMouseMoved(MouseEvent mouse) {
        // snap to mouse grid if needed
        int x = snapCoordinateToGrid(mouse.getX());
        int y = snapCoordinateToGrid(mouse.getY());
        if (mouse.isShiftDown() && robot != null) robot.mouseMove(x, y);

        // move flying canvas with a component
        if (holdingComp) {
            fly.setTranslateX(mouse.getX() - fly.getWidth() / 2.0);
            fly.setTranslateY(mouse.getY() - fly.getHeight() / 2.0);
        }

        // lay out flying wire
        if (holdingWire) {
            flyWire.layoutAgain(x, y);
            updateFlyWire();
        }
    }
    @FXML private void fieldMouseClicked(MouseEvent mouse) {
        // snap to mouse grid if needed
        int x = snapCoordinateToGrid(mouse.getX());
        int y = snapCoordinateToGrid(mouse.getY());
        if (holdingWire) {
            flyWire.layoutAgain(x, y);
            circuit.add(flyWire);
            holdingWire = false;
            fly.setVisible(false);
        } else if (holdingComp) {
            flyComp.setPos(x, y);
            circuit.add(flyComp);
        } else {
            Informative pointed = circuit.getPointedObject(x, y);
            if (pointed != null) {
                if (mouse.getButton() == MouseButton.PRIMARY) showInfoToolTip(x, y, pointed);
                else if (mouse.getButton() == MouseButton.SECONDARY) showActionToolTip(x, y, pointed);
            }
        }
    }
    @FXML private void fieldKeyPressed(KeyEvent key) {
        KeyCode code = key.getCode();
        if (code == KeyCode.ESCAPE && (holdingWire || holdingComp)) {
            holdingWire = false;
            holdingComp = false;
            fly.setVisible(false);
        } else if (code == KeyCode.SPACE && holdingWire) {
            flyWire.flip();
            updateFlyWire();
        }
        if (holdingComp) {
            if (code == KeyCode.R) flyComp.rotateCounterClockwise();
            else if (code == KeyCode.L) flyComp.rotateClockwise();
            else if (code == KeyCode.X) flyComp.mirrorHorizontal();
            else if (code == KeyCode.Y) flyComp.mirrorVertical();
        }
    }

    // object insertion logic
    private void updateGridParameters() {
        field.setWidth(p * w);
        field.setHeight(p * h);
    }
    private void updateFlyWire() {
        assert holdingWire && !holdingComp;

        // measure the canvas
        double fcw = flyWire.getWidth() * p;
        double fch = flyWire.getHeight() * p;
        fly.setWidth(fcw);
        fly.setHeight(fch);

        // render the wire
        GraphicsContext gc = fly.getGraphicsContext2D();
        gc.clearRect(0, 0, fcw, fch);
        gc.scale(p, p);
        flyWire.render(gc);
        gc.scale(1.0/p, 1.0/p);
    }
    private void updateFlyComp() {
        assert holdingComp && !holdingWire;

        // measure the flying canvas
        double fcw = flyComp.getWidth() * p;
        double fch = flyComp.getHeight() * p;
        fly.setWidth(fcw);
        fly.setHeight(fch);

        // render the component
        GraphicsContext gc = fly.getGraphicsContext2D();
        gc.clearRect(0, 0, fcw, fch);
        gc.scale(p, p);
        flyComp.render(gc);
        gc.scale(1.0/p, 1.0/p);
    }
    private void renderField() {
        // begin rendering
        GraphicsContext gc = field.getGraphicsContext2D();
        gc.scale(p, p);
        gc.setLineWidth(LINE_WIDTH);

        // clear background
        gc.setFill(COL_CLEAR);
        gc.fillRect(0.0, 0.0, w, h);

        // draw grid points
        gc.setFill(COL_GRID);
        for (int i = 0; i < w; i++)
            for (int j = 0; j < h; j++)
                gc.fillOval(i, j, GRID_POINT_RADIUS, GRID_POINT_RADIUS);

        // render circuit
        circuit.renderAll(gc);

        // finish rendering
        gc.scale(1.0/p, 1.0/p);
    }

    // dialogs & tooltips
    private void showGridSizeDialog() {
        // init stage partially
        Stage dlg = new Stage();
        dlg.initStyle(StageStyle.UNDECORATED);

        // construct gui
        Label lblWidth = new Label("Grid width:");
        Label lblHeight = new Label("Grid height:");
        TextField txtWidth = new TextField(Integer.toString(w));
        TextField txtHeight = new TextField(Integer.toString(h));
        Button btnConfirm = new Button("Confirm");
        Button btnCancel = new Button("Cancel");

        // set font
        lblWidth.setStyle(defFont);
        lblHeight.setStyle(defFont);
        txtWidth.setStyle(defFont);
        txtHeight.setStyle(defFont);
        btnConfirm.setStyle(defFont);
        btnCancel.setStyle(defFont);

        // activate buttons and text filters
        btnConfirm.setOnAction(event -> {
            String textW = txtWidth.getText();
            String textH = txtHeight.getText();
            try {
                int intW = Integer.parseInt(textW);
                int intH = Integer.parseInt(textH);
                // check bounds
                if (intW < MIN_GRID_SIZE || intW > MAX_GRID_SIZE || intH < MIN_GRID_SIZE || intH > MAX_GRID_SIZE) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("TERILOG");
                    alert.setHeaderText("Grid size bounds:");
                    alert.setContentText(String.format("Grid width and height must be integers between %d and %d.", MIN_GRID_SIZE, MAX_GRID_SIZE));
                    alert.showAndWait();
                } else {
                    w = intW;
                    h = intH;
                    updateGridParameters();
                    renderField();
                    dlg.close();
                }
            } catch (NumberFormatException e) {
                System.err.println("WARNING: grid width and height must be integers.");
            }
        });
        btnCancel.setOnAction(event -> dlg.close());
        txtWidth.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
                txtWidth.setText(newValue.replaceAll("[^\\d]", ""));
        });
        txtHeight.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*"))
                txtHeight.setText(newValue.replaceAll("[^\\d]", ""));
        });

        // construct gui
        HBox line1 = new HBox(lblWidth, txtWidth);
        HBox line2 = new HBox(lblHeight, txtHeight);
        HBox line3 = new HBox(btnConfirm, btnCancel);
        VBox root = new VBox(line1, line2, line3);

        // setup and show
        Scene scene = new Scene(root, root.getWidth(), root.getHeight());
        dlg.setScene(scene);
        dlg.showAndWait();
    }
    private void showInfoToolTip(double x, double y, Informative object) {

    }
    private void showActionToolTip(double x, double y, Informative object) {

    }

    // menu.file
    @FXML private void menuOpen() {

    }
    @FXML private void menuSave() {

    }
    @FXML private void menuSaveAs() {

    }
    @FXML private void menuToggle() {
        stage.setFullScreen(!stage.isFullScreen());
    }
    @FXML private void menuQuit() {
        Platform.exit();
        System.exit(0);
    }

    // menu.add
    @FXML private void menuHardN() {
        // modify flags
        holdingWire = false;
        holdingComp = true;

        // prepare flying canvas
        fly.setVisible(true);

        // begin insertion
        flyComp = new HardN(); // init flying component
        updateFlyComp(); // setup flying canvas
    }
    @FXML private void menuHardP() {}
    @FXML private void menuSoftN() {}
    @FXML private void menuSoftP() {}
    @FXML private void menuVoltage() {}
    @FXML private void menuIndicator() {}

    // menu.simulate
    @FXML private void menuPlay() {
        if (circuit.isSimulationRunning()) {
            circuit.stopSimulation();
            menuPlay.setText("Start");
        } else {
            circuit.startSimulation();
            menuPlay.setText("Stop");
        }
    }
    @FXML private void menuSettings() {}

    // menu.grid
    @FXML private void menuGrid() {
        showGridSizeDialog();
    }
    @FXML private void menuZoomIn() {
        double newPeriod = p * 1.12;
        if (newPeriod <= maxPeriod) {
            p = newPeriod;
            updateGridParameters();
            renderField();
            menuZoomOut.setDisable(false);
        } else {
            menuZoomIn.setDisable(true);
        }
    }
    @FXML private void menuZoomOut() {
        double newPeriod = p * 0.88;
        if (newPeriod >= minPeriod) {
            p = newPeriod;
            updateGridParameters();
            renderField();
            menuZoomIn.setDisable(false);
        } else {
            menuZoomOut.setDisable(true);
        }
    }

    // menu.help
    @FXML private void menuAbout() {}
    @FXML private void menuCredits() {}

    // informative
    public int getGridWidth() {
        return w;
    }
    public int getGridHeight() {
        return h;
    }
    public void setGridDimensions(int width, int height) {
        w = width;
        h = height;
        updateGridParameters();
        renderField();
    }

    // utilities
    private static int rnd(double val) {
        return (int) Math.round(val);
    }
    private int snapCoordinateToGrid(double c) {
        return rnd(Math.round(c / p) * p);
    }

}
