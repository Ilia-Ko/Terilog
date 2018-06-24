package gui.control;

import engine.Circuit;
import engine.TerilogIO;
import engine.components.Component;
import engine.components.arithmetic.FullAdder;
import engine.components.arithmetic.HalfAdder;
import engine.components.logic.one_arg.NTI;
import engine.components.logic.one_arg.PTI;
import engine.components.logic.one_arg.STI;
import engine.components.logic.two_arg.*;
import engine.components.lumped.Diode;
import engine.components.lumped.Indicator;
import engine.components.lumped.Reconciliator;
import engine.components.lumped.Voltage;
import engine.components.mosfets.HardN;
import engine.components.mosfets.HardP;
import engine.components.mosfets.SoftN;
import engine.components.mosfets.SoftP;
import engine.wires.FlyWire;
import gui.Main;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

public class ControlMain {

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
    private double defSpacing; // in pixels
    private String defFont;

    // circuit logic
    private Circuit circuit;
    private TerilogIO ioSystem;
    private File lastSave;

    // mouse logic
    private IntegerProperty mouseX, mouseY;
    private boolean holdingWire, holdingComp;
    private FlyWire flyWire;
    private Component flyComp;

    // initialization
    public void initialSetup(Stage stage, String defFont, double screenWidth, double screenHeight) {
        this.stage = stage;
        this.defFont = defFont;

        // dimensions
        p = new SimpleDoubleProperty(screenWidth * 0.01);
        w = new SimpleIntegerProperty((int) Math.round(screenWidth / p.get()));
        h = new SimpleIntegerProperty((int) Math.round(screenHeight / p.get()));
        minPeriod = p.get() / 12.0; if (minPeriod < 1.0) minPeriod = 1.0;
        maxPeriod = p.get() * 12.0;
        defSpacing = screenWidth * 0.005;

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
        parent.layoutXProperty().bindBidirectional(field.layoutXProperty());
        parent.layoutYProperty().bindBidirectional(field.layoutYProperty());

        // init 'point'
        Circle point = new Circle(0.5);
        point.setFill(Color.TRANSPARENT);
        point.setStroke(Color.DARKRED);
        point.setStrokeWidth(0.1);
        point.setMouseTransparent(true);
        point.visibleProperty().bind(parent.hoverProperty());
        point.centerXProperty().bind(mouseX);
        point.centerYProperty().bind(mouseY);
        parent.getChildren().add(point);

        // init circuit
        setCircuit(new Circuit());
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
        gc.setLineWidth(0.1);

        // clear background
        gc.setFill(Color.LIGHTCYAN);
        gc.fillRect(0.0, 0.0, w.doubleValue(), h.doubleValue());

        // draw grid points
        double a = 1.0 / 18.0;
        gc.setFill(Color.BLACK);
        for (int i = 0; i <= w.get(); i++)
            for (int j = 0; j <= h.get(); j++)
                gc.fillOval(i - a, j - a, 2 * a, 2 * a);

        // finish rendering
        gc.restore();
    }
    private Stage initDialog(FXMLLoader loader) throws IOException {
        GridPane root = loader.load();
        root.setStyle(defFont);
        root.setHgap(defSpacing);
        root.setVgap(defSpacing);
        root.setPadding(new Insets(defSpacing, defSpacing, defSpacing, defSpacing));
        Scene scene = new Scene(root);
        Stage dialog = new Stage(StageStyle.UNDECORATED);
        dialog.setScene(scene);
        return dialog;
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

        // stop panning by left-button drag
        if (mouse.isDragDetect() && mouse.getButton() == MouseButton.PRIMARY) mouse.consume();
    }
    private void onGlobalMouseClicked(MouseEvent mouse) {
        if (mouse.getButton() == MouseButton.PRIMARY) {
            if (holdingWire) {
                flyWire.confirm();
                holdingWire = false;
            } else if (holdingComp) {
                flyComp.confirm();
                holdingComp = false;
            }
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
                case DELETE:
                    flyComp.delete(false);
                    break;
                case CLOSE_BRACKET:
                    flyComp.rotateCW();
                    break;
                case OPEN_BRACKET:
                    flyComp.rotateCCW();
                    break;
                case QUOTE:
                    flyComp.mirrorX();
                    break;
                case BACK_SLASH:
                    flyComp.mirrorY();
                    break;
            }
        }
    }
    private void breakInsertion() {
        if (holdingComp) {
            flyComp.delete(false);
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
        if (tlg == null || !tlg.exists()) return;
        lastSave = tlg;

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
        File save = chooser.showSaveDialog(stage);
        if (save == null) return;
        lastSave = save;

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

    // menu.add.mosfet
    @FXML private void menuHardN() {
        breakInsertion();
        flyComp = new HardN(this);
        holdingComp = true;
    }
    @FXML private void menuHardP() {
        breakInsertion();
        flyComp = new HardP(this);
        holdingComp = true;
    }
    @FXML private void menuSoftN() {
        breakInsertion();
        flyComp = new SoftN(this);
        holdingComp = true;
    }
    @FXML private void menuSoftP() {
        breakInsertion();
        flyComp = new SoftP(this);
        holdingComp = true;
    }
    // menu.add.lumped
    @FXML private void menuDiode() {
        breakInsertion();
        flyComp = new Diode(this);
        holdingComp = true;
    }
    @FXML private void menuReconciliator() {
        breakInsertion();
        flyComp = new Reconciliator(this);
        holdingComp = true;
    }
    @FXML private void menuVoltage() {
        breakInsertion();
        flyComp = new Voltage(this);
        holdingComp = true;
    }
    @FXML private void menuIndicator() {
        breakInsertion();
        flyComp = new Indicator(this);
        holdingComp = true;
    }
    // menu.add.logic 1-arg
    @FXML private void menuNTI() {
        breakInsertion();
        flyComp = new NTI(this);
        holdingComp = true;
    }
    @FXML private void menuSTI() {
        breakInsertion();
        flyComp = new STI(this);
        holdingComp = true;
    }
    @FXML private void menuPTI() {
        breakInsertion();
        flyComp = new PTI(this);
        holdingComp = true;
    }
    // menu.add.logic 2-arg
    @FXML private void menuNAND() {
        breakInsertion();
        flyComp = new NAND(this);
        holdingComp = true;
    }
    @FXML private void menuNOR() {
        breakInsertion();
        flyComp = new NOR(this);
        holdingComp = true;
    }
    @FXML private void menuNCON() {
        breakInsertion();
        flyComp = new NCON(this);
        holdingComp = true;
    }
    @FXML private void menuNANY() {
        breakInsertion();
        flyComp = new NANY(this);
        holdingComp = true;
    }
    @FXML private void menuOKEY() {
        breakInsertion();
        flyComp = new OKEY(this);
        holdingComp = true;
    }
    @FXML private void menuCKEY() {
        breakInsertion();
        flyComp = new CKEY(this);
        holdingComp = true;
    }
    // menu.add.arithmetic
    @FXML private void menuHalfAdder() {
        breakInsertion();
        flyComp = new HalfAdder(this);
        holdingComp = true;
    }
    @FXML private void menuFullAdder() {
        breakInsertion();
        flyComp = new FullAdder(this);
        holdingComp = true;
    }
    // menu.add.wire
    @FXML private void menuWire() {
        breakInsertion();
        flyWire = new FlyWire(this);
        holdingWire = true;
    }

    // menu.circuit
    @FXML private void menuPlay() {
        if (circuit.getSimRunProperty().get()) circuit.stopSimulation();
        else circuit.startSimulation();
    }
    @FXML private void menuSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/dialogs/settings.fxml"));
            Stage dialog = initDialog(loader);
            ((ControlSettings) loader.getController()).initialSetup(dialog, this);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to show Simulation Settings dialog.");
        }
    }

    // menu.grid
    @FXML private void menuGrid() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/dialogs/grid.fxml"));
            Stage dialog = initDialog(loader);
            ((ControlGrid) loader.getController()).initialSetup(dialog, this);
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
    @FXML private void menuAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/dialogs/about.fxml"));
            Stage dialog = initDialog(loader);
            ((ControlAbout) loader.getController()).initialSetup(dialog);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to show About dialog.");
        }
    }
    @FXML private void menuCredits() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("view/dialogs/credits.fxml"));
            Stage dialog = initDialog(loader);
            ((ControlCredits) loader.getController()).initialSetup(dialog);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to show Credits dialog.");
        }
    }

    // getters and setters
    public Circuit getCircuit() {
        return circuit;
    }
    public void setCircuit(Circuit circuit) {
        this.circuit = circuit;
        stage.setTitle(String.format("%s - %s", Main.TITLE, circuit.getNameProperty().get()));

        // bindings
        circuit.getSimRunProperty().addListener((observable, oldValue, newValue) -> menuPlay.setText(newValue ? "Stop" : "Start"));
        circuit.getNameProperty().addListener(((observable, oldValue, newValue) -> stage.setTitle(String.format("%s - %s", Main.TITLE, newValue))));
    }
    public Pane getParent() {
        return parent;
    }
    public IntegerProperty getMouseX() {
        return mouseX;
    }
    public IntegerProperty getMouseY() {
        return mouseY;
    }
    public void setFlyComp(Component comp) {
        flyComp = comp;
        holdingComp = true;
    }
    IntegerProperty getGridWidth() {
        return w;
    }
    IntegerProperty getGridHeight() {
        return h;
    }

    // xml info
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
    public Element writeGridToXML(Document doc) {
        Element g = doc.createElement("grid");
        g.setAttribute("w", Integer.toString(w.intValue()));
        g.setAttribute("h", Integer.toString(h.intValue()));
        return g;
    }
    public void readGridFromXML(Element c) {
        NodeList list = c.getElementsByTagName("grid");
        if (list.getLength() != 1) {
            System.out.println("WARNING: <circuit> entry should contain exactly one <grid> tag.");
            return;
        }
        Element g = (Element) list.item(0);
        w.setValue(Integer.parseInt(g.getAttribute("w")));
        h.setValue(Integer.parseInt(g.getAttribute("h")));
    }

}
