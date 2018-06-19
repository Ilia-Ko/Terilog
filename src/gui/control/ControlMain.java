package gui.control;

import engine.Circuit;
import engine.TerilogIO;
import engine.components.Component;
import engine.components.lumped.Diode;
import engine.components.lumped.Indicator;
import engine.components.lumped.Reconciliator;
import engine.components.lumped.Voltage;
import engine.components.mosfets.HardN;
import engine.components.mosfets.HardP;
import engine.components.mosfets.SoftN;
import engine.components.mosfets.SoftP;
import engine.connectivity.Wire;
import gui.Main;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

public class ControlMain {

    // dimensions
    private static final double GRID_PERIOD = 0.01; // relative to screen width
    private static final double GRID_POINT_RADIUS = 1.0 / 18.0; // in periods
    private static final double GRID_HOVER_RADIUS = 0.5; // in periods
    private static final double LINE_WIDTH = 1.0 / 12.0; // in periods
    private static final double GRID_PANE_GAP = 0.01;
    // input bounds
    private static final double MIN_GRID_PERIOD = 1.0 / 6.0; // relative to period
    private static final double MAX_GRID_PERIOD = 1.0 * 144.0; // relative to period

    // some gui
    private Stage stage;
    @FXML private ScrollPane scroll;
    @FXML private StackPane stack;
    @FXML private Canvas field; // background
    @FXML private Pane parent; // container for everything
    @FXML private MenuItem menuPlay;
    @FXML private MenuItem menuZoomIn;
    @FXML private MenuItem menuZoomOut;
    @FXML private Label lblPoint; // display snapped mouse position

    // dimensions
    private DoubleProperty p; // in pixels
    private IntegerProperty w, h; // in periods
    private double minPeriod, maxPeriod; // in pixels
    private double defSpacing;
    private String defFont;

    // circuit logic
    private Circuit circuit;
    private TerilogIO ioSystem;
    private File lastSave;

    // mouse logic
    private IntegerProperty mouseX, mouseY;
    private boolean holdingWire, holdingComp;
    private Wire flyWire;
    private engine.components.Component flyComp1;

    // initialization
    public void initialSetup(Stage stage, String defFont, double screenWidth, double screenHeight) {
        this.stage = stage;
        this.defFont = defFont;

        // dimensions
        p = new SimpleDoubleProperty(screenWidth * GRID_PERIOD);
        w = new SimpleIntegerProperty((int) Math.round(screenWidth / p.get()));
        h = new SimpleIntegerProperty((int) Math.round(screenHeight / p.get()));
        minPeriod = p.get() * MIN_GRID_PERIOD; if (minPeriod < 1.0) minPeriod = 1.0;
        maxPeriod = p.get() * MAX_GRID_PERIOD;
        defSpacing = screenWidth * GRID_PANE_GAP;

        // init events
        mouseX = new SimpleIntegerProperty();
        mouseY = new SimpleIntegerProperty();
        scroll.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouse -> {
            if (mouse.getButton() == MouseButton.PRIMARY)
                mouse.consume();
        });
        scroll.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onGlobalMouseClicked);
        scroll.addEventFilter(KeyEvent.KEY_PRESSED, this::onGlobalKeyPressed);
        stack.addEventFilter(MouseEvent.MOUSE_MOVED, this::onGlobalMouseMoved);

        // init field
        field.widthProperty().bind(w.multiply(p));
        field.heightProperty().bind(h.multiply(p));
        renderField();

        // init parent
        Scale scale = new Scale();
        scale.xProperty().bind(p);
        scale.yProperty().bind(p);
        parent.getTransforms().add(scale);
        parent.setCursor(Cursor.CROSSHAIR);
        parent.setOnMouseEntered(mouse -> parent.requestFocus());

        // init 'point'
        Circle point = new Circle(GRID_HOVER_RADIUS);
        point.setFill(Color.TRANSPARENT);
        point.setStroke(Color.DARKRED);
        point.setStrokeWidth(LINE_WIDTH);
        point.setMouseTransparent(true);
        point.visibleProperty().bind(parent.hoverProperty());
        point.centerXProperty().bind(mouseX);
        point.centerYProperty().bind(mouseY);
        parent.getChildren().add(point);

        // circuit logic
        circuit = new Circuit(this);
        try {
            ioSystem = new TerilogIO(this);
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
            ioSystem = null;
        }
        lastSave = null;
    }
    private void renderField() {
        // configure gc
        GraphicsContext gc = field.getGraphicsContext2D();
        gc.save();
        gc.scale(p.doubleValue(), p.doubleValue());
        gc.setLineWidth(LINE_WIDTH);

        // clear background
        gc.setFill(Color.LIGHTCYAN);
        gc.fillRect(0.0, 0.0, w.doubleValue(), h.doubleValue());

        // draw grid points
        double a = GRID_POINT_RADIUS;
        gc.setFill(Color.BLACK);
        for (int i = 0; i <= w.get(); i++)
            for (int j = 0; j <= h.get(); j++)
                gc.fillOval(i - a, j - a, 2 * a, 2 * a);

        // finish rendering
        gc.restore();
    }

    // some actions
    private void onGlobalMouseMoved(MouseEvent mouse) {
        // update coordinates (I don't know how to bind them!)
        int mx = (int) Math.round(mouse.getX() / p.get());
        int my = (int) Math.round(mouse.getY() / p.get());
        mouseX.setValue(mx);
        mouseY.setValue(my);

        // update mouse position label
        lblPoint.setText(String.format("%3d : %3d ", mx, my));

        // stop event (except for panning)
        if (mouse.getButton() != MouseButton.SECONDARY) mouse.consume();
    }
    private void onGlobalMouseClicked(MouseEvent mouse) {
        if (mouse.getButton() == MouseButton.PRIMARY) {
            // insertion logic
            if (holdingWire) {
                flyWire.confirm();
                holdingWire = false;
            } else if (holdingComp) {
                flyComp1.confirm();
                holdingComp = false;
            }

            // prevent dragging with primary button (but ScrollPane is still pannable with secondary button)
            mouse.consume();
        }
    }
    private void onGlobalKeyPressed(KeyEvent key) {
        // insertion logic
        KeyCode code = key.getCode();
        if (code == KeyCode.ESCAPE) { // stop insertion
            if (holdingComp || holdingWire)
                breakInsertion();
        } else if (code == KeyCode.SPACE && holdingWire) { // flip flying wire
            flyWire.flip();
            key.consume(); // prevent ScrollPane from receiving space key
        }
        if (holdingComp) {
            switch (code) {
                case INSERT:
                    moveComp(flyComp1);
                    break;
                case DELETE:
                    deleteComp(flyComp1);
                    break;
                case CLOSE_BRACKET:
                    flyComp1.rotateCW();
                    break;
                case OPEN_BRACKET:
                    flyComp1.rotateCCW();
                    break;
                case QUOTE:
                    flyComp1.mirrorX();
                    break;
                case BACK_SLASH:
                    flyComp1.mirrorY();
                    break;
            }
        }
    }
    private void moveComp(Component comp) {
        deleteComp(comp);
        flyComp1 = new HardN(this);
    }
    private void deleteComp(Component comp) {
        flyComp1.delete();
        circuit.del(comp);
    }

    // insertion
    private void beginCompInsertion() {
//        // setup basis
//        Canvas basis = flyComp.getBasis();
//        parent.getChildren().add(basis);
//        basis.setMouseTransparent(true);
//        basis.setCursor(Cursor.CLOSED_HAND);
//
//        // initialize component
//        flyComp.setGlobalAlpha(OPACITY_FLYING);
//        flyComp.setGridPeriod(p.doubleValue());
//        flyComp.setPos(mouseX.get(), mouseY.get());
//        flyComp.render();


        holdingComp = true;
    }
    private void breakInsertion() {
        if (holdingComp) {
            flyComp1.delete();
            holdingComp = false;
        } else if (holdingWire) {
            flyWire.delete();
            holdingWire = false;
        }
    }

    // menu.file
    @FXML private void menuOpen() {
        // configure file chooser
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open TLG file");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("TLG files", "*.tlg"));

        // get file
        File tlg = chooser.showOpenDialog(stage);

        // load it
        try {
            ioSystem.loadTLG(tlg);
        } catch (IOException e) {
            e.printStackTrace();
            // show IO alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Main.TITLE);
            alert.setHeaderText("IO Error:");
            alert.setContentText("Failed to open " + tlg.getAbsolutePath());
            alert.showAndWait();
        } catch (SAXException e) {
            e.printStackTrace();
            // show XML alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Main.TITLE);
            alert.setHeaderText("XML error:");
            alert.setContentText("Failed to parse " + tlg.getAbsolutePath());
            alert.showAndWait();
        }
    }
    @FXML private void menuSave() {
        if (lastSave == null)
            menuSaveAs();
        else
            updateSavedFile();
    }
    @FXML private void menuSaveAs() {
        // configure file chooser
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save TLG file");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("TLG files", "*.tlg"));

        // get file
        lastSave = chooser.showSaveDialog(stage);

        // save it
        updateSavedFile();
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
        breakInsertion();
        flyComp1 = new HardN(this);
        holdingComp = true;
    }
    @FXML private void menuHardP() {
        breakInsertion();
        flyComp1 = new HardP(this);
        holdingComp = true;
    }
    @FXML private void menuSoftN() {
        breakInsertion();
        flyComp1 = new SoftN(this);
        holdingComp = true;
    }
    @FXML private void menuSoftP() {
        breakInsertion();
        flyComp1 = new SoftP(this);
        holdingComp = true;
    }
    @FXML private void menuDiode() {
        breakInsertion();
        flyComp1 = new Diode(this);
        holdingComp = true;
    }
    @FXML private void menuReconciliator() {
        breakInsertion();
        flyComp1 = new Reconciliator(this);
        beginCompInsertion();
    }
    @FXML private void menuVoltage() {
        breakInsertion();
        flyComp1 = new Voltage(this);
        holdingComp = true;
    }
    @FXML private void menuIndicator() {
        breakInsertion();
        flyComp1 = new Indicator(this);
        beginCompInsertion();
    }
    @FXML private void menuWire() {
        breakInsertion();
        flyWire = new Wire(this);
        holdingWire = true;
    }

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
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/grid.fxml"));
        try {
            // init gui
            GridPane root = loader.load();
            root.setStyle(defFont);
            root.setHgap(defSpacing);
            root.setVgap(defSpacing);
            Scene scene = new Scene(root);
            Stage dialog = new Stage(StageStyle.UNDECORATED);
            dialog.setScene(scene);

            // init controller
            ControlDlgGrid control = loader.getController();
            control.initialSetup(dialog, this, w.get(), h.get());

            // show
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to show Grid Size dialog.");
        }
    }
    @FXML private void menuZoomIn() {
        double newPeriod = p.doubleValue() * 1.12;
        if (newPeriod <= maxPeriod) {
            p.setValue(newPeriod);
            renderField();
            menuZoomOut.setDisable(false);
        } else {
            menuZoomIn.setDisable(true);
        }
    }
    @FXML private void menuZoomOut() {
        double newPeriod = p.doubleValue() * 0.88;
        if (newPeriod >= minPeriod) {
            p.setValue(newPeriod);
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
        return w.get();
    }
    public int getGridHeight() {
        return h.get();
    }
    public void setGridDimensions(int width, int height) {
        w.setValue(width);
        h.setValue(height);
        renderField();
    }

    // misc
    public Pane getParent() {
        return parent;
    }
    public Circuit getCircuit() {
        return circuit;
    }
    public void setCircuit(Circuit circuit) {
        this.circuit = circuit;
    }
    public IntegerProperty getMouseX() {
        return mouseX;
    }
    public IntegerProperty getMouseY() {
        return mouseY;
    }
    public Element writeGridToXML(Document doc) {
        Element g = doc.createElement("grid");
        return g;
    }
    public void readGridFromXML(Element g) {

    }

    private void updateSavedFile() {
        try {
            ioSystem.saveTLG(lastSave);
        } catch (TransformerException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Main.TITLE);
            alert.setHeaderText("IO error:");
            alert.setContentText("Failed to save " + lastSave.getAbsolutePath());
            alert.showAndWait();
            lastSave = null;
        }
    }

}
