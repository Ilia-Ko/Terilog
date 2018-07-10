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
import engine.components.memory.*;
import engine.components.mosfets.HardN;
import engine.components.mosfets.HardP;
import engine.components.mosfets.SoftN;
import engine.components.mosfets.SoftP;
import engine.wires.FlyWire;
import gui.Main;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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
import java.util.Optional;
import java.util.Stack;

public class ControlMain {

    // some gui
    private Stage stage;
    @FXML private ScrollPane scroll;
    @FXML private StackPane stack;
    @FXML private Canvas field; // background
    @FXML private Pane parent; // container for everything
//    @FXML private MenuItem menuRun, menuStop;
    @FXML private MenuItem menuZoomIn;
    @FXML private MenuItem menuZoomOut;
    @FXML private MenuItem menuUndo;
    @FXML private MenuItem menuRedo;
    @FXML private Label lblPoint; // display snapped mouse position
//    @FXML private ProgressIndicator progress;

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
    // dragging and selecting
    private BooleanProperty isSelecting;
    private IntegerProperty selStartX, selStartY;
    private ContextMenu selMenu;

    // actions history
    private Stack<HistoricalEvent> histUndo, histRedo;

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
        selStartX = new SimpleIntegerProperty();
        selStartY = new SimpleIntegerProperty();
        isSelecting = new SimpleBooleanProperty(false);

        scroll.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouse -> {
            if (mouse.getButton() == MouseButton.PRIMARY) {
                if (!isSelecting.get()) {
                    isSelecting.setValue(true);
                    selStartX.setValue(mouseX.get());
                    selStartY.setValue(mouseY.get());
                }
                onGlobalMouseMoved(mouse);
                mouse.consume();
            }
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

        // init 'selection'
        Rectangle sel = new Rectangle();
        sel.setFill(Color.LIGHTGOLDENRODYELLOW);
        sel.setOpacity(0.8);
        sel.setStroke(Color.BLACK);
        sel.setStrokeWidth(0.1);
        sel.xProperty().bind(selStartX);
        sel.yProperty().bind(selStartY);
        sel.widthProperty().bind(mouseX.subtract(selStartX));
        sel.heightProperty().bind(mouseY.subtract(selStartY));
        sel.visibleProperty().bind(isSelecting);
        sel.setMouseTransparent(true);
        parent.getChildren().add(sel);
        scroll.addEventFilter(MouseEvent.MOUSE_RELEASED, mouse -> {
            if (isSelecting.get()) circuit.sel(sel);
            isSelecting.setValue(false);
        });

        // init selection menu
        MenuItem itemMove = new MenuItem("Move");
        itemMove.setOnAction(action -> circuit.selMove());
        MenuItem itemDel = new MenuItem("Remove");
        itemDel.setOnAction(action -> circuit.selDel());
        selMenu = new ContextMenu(itemMove, itemDel);
        parent.setOnContextMenuRequested(mouse -> {
            if (circuit.hasSelectedItems())
                selMenu.show(parent, mouse.getScreenX(), mouse.getScreenY());
        });

        // init history
        histUndo = new Stack<>();
        histRedo = new Stack<>();

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
        dialog.setTitle(Main.TITLE);
        dialog.setScene(scene);
        return dialog;
    }

    // some actions
    private void onGlobalMouseMoved(MouseEvent mouse) {
        // update coordinates
        int mx = (int) Math.round(mouse.getX() / p.get());
        int my = (int) Math.round(mouse.getY() / p.get());
        mouseX.setValue(mx);
        mouseY.setValue(my);

        // update mouse position label
        lblPoint.setText(String.format("%3d : %3d ", mx, my));
    }
    private void onGlobalMouseClicked(MouseEvent mouse) {
        if (mouse.getButton() == MouseButton.PRIMARY) {
            if (holdingWire) {
                flyWire.confirm();
                holdingWire = false;
            } else if (holdingComp) {
                flyComp.confirm();
                holdingComp = false;
            } else if (circuit.isSelectionMoving()) circuit.selStop();
        }
    }
    private void onGlobalKeyPressed(KeyEvent key) {
        // insertion logic
        KeyCode code = key.getCode();
        if (code == KeyCode.ESCAPE) { // stop insertion or selection
            if (holdingComp || holdingWire) breakInsertion();
            if (circuit.hasSelectedItems()) circuit.unsel();
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
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("XML files", "*.xml"));

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
            alert.getDialogPane().setStyle(defFont);
            alert.setResizable(true);
            alert.setTitle(Main.TITLE);
            alert.setHeaderText("IO Error:");
            alert.setContentText("Failed to open " + tlg.getAbsolutePath());
            alert.showAndWait();
        } catch (SAXException e) {
            e.printStackTrace();
            // show XML alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().setStyle(defFont);
            alert.setResizable(true);
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
        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("XML files", "*.xml"));

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

    // menu.edit
    @FXML private void menuUndo() {
        if (!histUndo.empty()) {
            HistoricalEvent action = histUndo.pop();
            menuUndo.setDisable(histUndo.empty());
            histRedo.push(action);
            menuRedo.setDisable(false);
            action.undo();
        }
    }
    @FXML private void menuRedo() {
        if (!histRedo.empty()) {
            HistoricalEvent action = histRedo.pop();
            menuRedo.setDisable(histRedo.empty());
            histUndo.push(action);
            menuUndo.setDisable(false);
            action.redo();
        }
    }
    public void appendHistory(HistoricalEvent event) {
        histUndo.push(event);
        menuUndo.setDisable(false);
    }
    public void rewriteHistory(HistoricalEvent event) {
        histUndo.remove(event);
        histRedo.remove(event);
        menuUndo.setDisable(histUndo.empty());
        menuRedo.setDisable(histRedo.empty());
    }
    public void forgetHistory() {
        histUndo.clear();
        histRedo.clear();
        menuUndo.setDisable(true);
        menuRedo.setDisable(true);
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
    // menu.add.memory
    @FXML private void menuTrigger() {
        breakInsertion();
        flyComp = new Trigger(this);
        holdingComp = true;
    }
    @FXML private void menuTriplet() {
        breakInsertion();
        flyComp = new Triplet(this);
        holdingComp = true;
    }
    @FXML private void menuTryte() {
        breakInsertion();
        flyComp = new Tryte(this);
        holdingComp = true;
    }
    @FXML private void menuWord() {
        breakInsertion();
        flyComp = new Word(this);
        holdingComp = true;
    }
    @FXML private void menuDword() {
        breakInsertion();
        flyComp = new Dword(this);
        holdingComp = true;
    }
    // menu.add.wire
    @FXML private void menuWire() {
        breakInsertion();
        flyWire = new FlyWire(this);
        holdingWire = true;
    }

    // menu.circuit
    @FXML private void menuReset() {
        boolean doReset = true;

        // ask confirmation
        if (circuit.isReallyBig()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.getDialogPane().setStyle(defFont);
            alert.setResizable(true);
            alert.setTitle(Main.TITLE);
            alert.setHeaderText("Reset");
            alert.setContentText("Are you sure to reset the circuit? After reset action circuit requires reparsing which may take some time.");

            // reset if OK
            Optional<ButtonType> res = alert.showAndWait();
            doReset = res.isPresent() && res.get() == ButtonType.OK;
        }

        // reset
        if (doReset) circuit.doReset();
    }
    @FXML private void menuParse() {
        boolean doParsing = true;

        // ask confirmation
        if (!circuit.hasToBeParsed() && circuit.isReallyBig()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.getDialogPane().setStyle(defFont);
            alert.setResizable(true);
            alert.setTitle(Main.TITLE);
            alert.setHeaderText("Parsing");
            alert.setContentText("Circuit is already parsed. Are you sure to begin reparsing? This may take some time.");
            Optional<ButtonType> res = alert.showAndWait();
            doParsing = res.isPresent() && res.get() == ButtonType.OK;
        }

        // (re)parse
        if (doParsing) circuit.doParse();
    }
    @FXML private void menuStepInto() {
        circuit.doStepInto();
    }
    @FXML private void menuStepOver() {
        circuit.doStepOver();

        // warn if simulation is not finished
        if (!circuit.wasFinished()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.getDialogPane().setStyle(defFont);
            alert.setResizable(true);
            alert.setTitle(Main.TITLE);
            alert.setHeaderText("Step over");
            alert.setContentText("Failed to stabilize circuit. Increase simulation depth or make few more steps into.");
            alert.showAndWait();
        }
    }
    @FXML private void menuRun() {

    }
    @FXML private void menuStop() {

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
            VBox root = loader.load();
            root.setStyle(defFont);
            root.setSpacing(defSpacing);
            root.setPadding(new Insets(defSpacing, defSpacing, defSpacing, defSpacing));
            Scene scene = new Scene(root);
            Stage dialog = new Stage(StageStyle.UNDECORATED);
            dialog.setTitle(Main.TITLE);
            dialog.setScene(scene);
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
        stage.setTitle(String.format("%s - %s", Main.TITLE, circuit.nameProperty().get()));

        // bindings
        circuit.nameProperty().addListener(((observable, oldName, newName) -> stage.setTitle(String.format("%s - %s", Main.TITLE, newName))));
//        menuRun.disableProperty().bind(circuit.taskRunProperty());
//        menuStop.disableProperty().bind(circuit.taskRunProperty().not());
//        progress.visibleProperty().bind(circuit.taskRunProperty());
    }
    public Pane getParent() {
        return parent;
    }
    public String getDefaultFont() {
        return defFont;
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
            alert.getDialogPane().setStyle(defFont);
            alert.setResizable(true);
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
