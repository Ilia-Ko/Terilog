package engine.components;

import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Bloom;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

public abstract class Component {

    // layout
    protected Pane root;
    private ControlMain control;
    private Rotate rotate;
    private Scale scale;

    // initialization
    protected Component(ControlMain control) { // create Component in layout mode
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

        begin();
    }
    protected Component(ControlMain control, Element data) { // create Component from xml in main mode
        this(control);
        readXML(data);
        confirm();
    }
    protected Pane loadContent() {
        try {
            String location = "view/components/lumped/" + getAttrClass() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    protected ContextMenu buildContextMenu() {
        // move
        MenuItem itemMove = new MenuItem("Move");
        itemMove.setAccelerator(KeyCombination.valueOf("Insert"));
        itemMove.setOnAction(event -> {
            control.getCircuit().del(this);
            begin();
            control.setFlyComp(this);
        });

        // delete
        MenuItem itemDelete = new MenuItem("Remove");
        itemDelete.setAccelerator(KeyCombination.valueOf("Delete"));
        itemDelete.setOnAction(event -> delete());

        // rotate clockwise
        MenuItem itemRotCW = new MenuItem("Rotate right (CW)");
        itemRotCW.setAccelerator(KeyCombination.valueOf("]"));
        itemRotCW.setOnAction(event -> rotateCW());

        // rotate counterclockwise
        MenuItem itemRotCCW = new MenuItem("Rotate left (CCW)");
        itemRotCCW.setAccelerator(KeyCombination.valueOf("["));
        itemRotCCW.setOnAction(event -> rotateCCW());

        // mirror x
        MenuItem itemMirrorX = new MenuItem("Mirror X");
        itemMirrorX.setAccelerator(KeyCombination.valueOf("\""));
        itemMirrorX.setOnAction(event -> mirrorX());

        // mirror y
        MenuItem itemMirrorY = new MenuItem("Mirror Y");
        itemMirrorY.setAccelerator(KeyCombination.valueOf("\\"));
        itemMirrorY.setOnAction(event -> mirrorY());

        return new ContextMenu(itemMove, itemDelete, itemRotCW, itemRotCCW, itemMirrorX, itemMirrorY);
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
        Tooltip.install(root, new Tooltip(String.format("%s #%s", getAttrClass(), root.getId())));

        control.getCircuit().add(this);
    }
    public void delete() {
        control.getParent().getChildren().remove(root);
        control.getCircuit().del(this);
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
    public abstract void simulate();

    // xml info
    public Element writeXML(Document doc) {
        Element comp = doc.createElement("comp");
        comp.setAttribute("class", getAttrClass());
        comp.setAttribute("id", root.getId());
        comp.setAttribute("x", asInt(root.layoutXProperty()));
        comp.setAttribute("y", asInt(root.layoutYProperty()));
        comp.setAttribute("rot", asInt(root.rotateProperty()));
        comp.setAttribute("mx", asInt(root.scaleXProperty()));
        comp.setAttribute("my", asInt(root.scaleYProperty()));
        return comp;
    }
    protected void readXML(Element comp) {
        root.setId(comp.getAttribute("id"));
        root.setLayoutX(getDouble(comp, "x"));
        root.setLayoutY(getDouble(comp, "y"));
        root.setRotate(getDouble(comp, "rot"));
        root.setScaleX(getDouble(comp, "mx"));
        root.setScaleY(getDouble(comp, "my"));
    }
    protected abstract String getAttrClass();

    // utils
    private static String asInt(DoubleProperty d) {
        return Integer.toString(d.intValue());
    }
    private static double getDouble(Element from, String what) {
        return Double.parseDouble(from.getAttribute(what));
    }

}
