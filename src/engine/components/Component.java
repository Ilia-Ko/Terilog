package engine.components;

import engine.connectivity.Node;
import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Bloom;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

public abstract class Component {

    private Pane root;
    private ControlMain control;
    private Rotate rotate;
    private Scale scale;
    private HashSet<Pin> pins;

    // initialization
    private Component(ControlMain control, boolean isLayoutMode) {
        this.control = control;
        root = loadContent();
        root.setId(Integer.toString(hashCode()));
        control.getParent().getChildren().add(root);

        // attach context menu
        ContextMenu menu = buildContextMenu();
        root.setOnContextMenuRequested(mouse -> menu.show(root, mouse.getScreenX(), mouse.getScreenY()));

        // hover effect
        root.hoverProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                root.setEffect(new Bloom(0.7));
            else
                root.setEffect(null);
        });

        // layout transforms
        rotate = new Rotate(0, 0, 0);
        scale = new Scale(1, 1, 0, 0);
        root.getTransforms().addAll(rotate, scale);

        pins = initPins();
        if (isLayoutMode) begin();
    }
    protected Component(ControlMain control) { // create Component in layout mode
        this(control, true);
    }
    protected Component(ControlMain control, Element data) { // create Component from xml in main mode
        this(control, false);
        readXML(data);
        confirm();
    }
    protected Pane loadContent() {
        try {
            String location = "view/components/lumped/" + getClass().getSimpleName().toLowerCase() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    protected ContextMenu buildContextMenu() {
        // move
        MenuItem itemMove = new MenuItem("Move");
//        itemMove.setAccelerator(KeyCombination.valueOf("Insert"));
        itemMove.setOnAction(event -> {
            control.getCircuit().del(this);
            begin();
            control.setFlyComp(this);
        });

        // delete
        MenuItem itemDelete = new MenuItem("Remove");
//        itemDelete.setAccelerator(KeyCombination.valueOf("Delete"));
        itemDelete.setOnAction(event -> delete(true));

        // rotate clockwise
        MenuItem itemRotCW = new MenuItem("Rotate right (CW)");
//        itemRotCW.setAccelerator(KeyCombination.valueOf("]"));
        itemRotCW.setOnAction(event -> rotateCW());

        // rotate counterclockwise
        MenuItem itemRotCCW = new MenuItem("Rotate left (CCW)");
//        itemRotCCW.setAccelerator(KeyCombination.valueOf("["));
        itemRotCCW.setOnAction(event -> rotateCCW());

        // mirror x
        MenuItem itemMirrorX = new MenuItem("Mirror X");
//        itemMirrorX.setAccelerator(KeyCombination.valueOf("\""));
        itemMirrorX.setOnAction(event -> mirrorX());

        // mirror y
        MenuItem itemMirrorY = new MenuItem("Mirror Y");
//        itemMirrorY.setAccelerator(KeyCombination.valueOf("\\"));
        itemMirrorY.setOnAction(event -> mirrorY());

        return new ContextMenu(itemMove, itemDelete, itemRotCW, itemRotCCW, itemMirrorX, itemMirrorY);
    }
    protected HashSet<Pin> initPins() {
        return new HashSet<>();
    }

    // layout mode
    private void begin() {
        root.setOpacity(0.4);
        root.layoutXProperty().bind(control.getMouseX());
        root.layoutYProperty().bind(control.getMouseY());
    }
    public void confirm() {
        root.layoutXProperty().unbind();
        root.layoutYProperty().unbind();

        root.setOpacity(1.0);
        root.setOnMouseEntered(mouse -> root.requestFocus());
        Tooltip.install(root, new Tooltip(getClass().getSimpleName()));

        control.getCircuit().add(this);
    }
    public void delete(boolean fromCircuit) {
        if (fromCircuit) control.getCircuit().del(this);
        control.getParent().getChildren().remove(root);
    }

    // layout transforms
    public void rotateCW() {
        rotate.setAngle(rotate.getAngle() + 90.0);
    }
    public void rotateCCW() {
        rotate.setAngle(rotate.getAngle() - 90.0);
    }
    public void mirrorX() {
        scale.setX(-scale.getX());
    }
    public void mirrorY() {
        scale.setY(-scale.getY());
    }

    // simulation
    public boolean isEntryPoint() {
        return false;
    }
    public abstract HashSet<Node> simulate(); // should return list of affected nodes

    // xml info
    public Element writeXML(Document doc) {
        Element comp = doc.createElement("comp");
        comp.setAttribute("class", getClass().getSimpleName().toLowerCase());
        comp.setAttribute("x", asInt(root.layoutXProperty()));
        comp.setAttribute("y", asInt(root.layoutYProperty()));
        comp.setAttribute("rot", asInt(rotate.angleProperty()));
        comp.setAttribute("mx", asInt(scale.xProperty()));
        comp.setAttribute("my", asInt(scale.yProperty()));
        return comp;
    }
    protected void readXML(Element comp) {
        root.setLayoutX(getDouble(comp, "x"));
        root.setLayoutY(getDouble(comp, "y"));
        rotate.setAngle(getDouble(comp, "rot"));
        scale.setX(getDouble(comp, "mx"));
        scale.setY(getDouble(comp, "my"));
    }

    // misc
    protected Pane getRoot() {
        return root;
    }
    protected Rotate getRotation() {
        return rotate;
    }
    protected Scale getScale() {
        return scale;
    }
    public HashSet<Pin> getPins() {
        return pins;
    }
    private static String asInt(DoubleProperty d) {
        return Integer.toString(d.intValue());
    }
    private static double getDouble(Element from, String what) {
        return Double.parseDouble(from.getAttribute(what));
    }

}
