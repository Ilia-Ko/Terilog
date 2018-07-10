package engine.components;

import engine.connectivity.Node;
import engine.connectivity.Selectable;
import gui.Main;
import gui.control.ControlMain;
import gui.control.HistoricalEvent;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.HashSet;

public abstract class Component implements Selectable {

    private Pane root;
    private ControlMain control;
    private Rotate rotate;
    private Scale scale;
    private HashSet<Pin> pins;
    private BooleanProperty isSelected;
    private HistoricalEvent toBeOrNotToBe;

    // initialization
    private Component(ControlMain control, boolean isLayoutMode) {
        this.control = control;
        root = loadContent();
        root.setId(Integer.toString(hashCode()));
        control.getParent().getChildren().add(root);
        isSelected = new SimpleBooleanProperty(false);

        // attach context menu
        ContextMenu menu = buildContextMenu();
        root.setOnContextMenuRequested(mouse -> {
            if (isSelected.not().get())
                menu.show(root, mouse.getScreenX(), mouse.getScreenY());
        });

        // selection effect
        isSelected.addListener((observable, wasSelected, nowSelected) -> root.setEffect(nowSelected ? HIGHLIGHT : null));

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
        itemMove.setOnAction(event -> {
            control.rewriteHistory(toBeOrNotToBe);
            control.getCircuit().del(this);
            begin();
            control.setFlyComp(this);
        });

        // delete
        MenuItem itemDelete = new MenuItem("Remove");
        itemDelete.setOnAction(event -> {
            control.rewriteHistory(toBeOrNotToBe);
            delete(true);
            control.appendHistory(HistoricalEvent.invert(toBeOrNotToBe));
        });

        // rotate clockwise
        MenuItem itemRotCW = new MenuItem("Rotate right (CW)");
        itemRotCW.setOnAction(event -> rotateCW());

        // rotate counterclockwise
        MenuItem itemRotCCW = new MenuItem("Rotate left (CCW)");
        itemRotCCW.setOnAction(event -> rotateCCW());

        // mirror x
        MenuItem itemMirrorX = new MenuItem("Mirror X");
        itemMirrorX.setOnAction(event -> mirrorX());

        // mirror y
        MenuItem itemMirrorY = new MenuItem("Mirror Y");
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
        Tooltip.install(root, new Tooltip(getClass().getSimpleName()));

        control.getCircuit().add(this);

        final Component me = this;
        toBeOrNotToBe = new HistoricalEvent() {
            @Override public void undo() {
                delete(true);
            }
            @Override public void redo() {
                control.getParent().getChildren().add(me.root);
                control.getCircuit().add(me);
            }
        };
        control.appendHistory(toBeOrNotToBe);
    }
    public void delete(boolean fromCircuit) {
        if (fromCircuit) control.getCircuit().del(this);
        control.getParent().getChildren().remove(root);
    }

    // selection
    @Override public boolean checkSelection(Rectangle sel) {
        isSelected.setValue(sel.intersects(root.getBoundsInParent()));
        return isSelected.get();
    }
    @Override public void breakSelection() {
        isSelected.setValue(false);
    }
    @Override public void delete() {
        delete(true);
        isSelected.setValue(false);
    }
    @Override public void move() {
        control.getCircuit().del(this);
        root.setOpacity(0.4);

        // move
        IntegerProperty dX = new SimpleIntegerProperty(root.layoutXProperty().intValue() - control.getMouseX().get());
        IntegerProperty dY = new SimpleIntegerProperty(root.layoutYProperty().intValue() - control.getMouseY().get());
        root.layoutXProperty().bind(control.getMouseX().add(dX));
        root.layoutYProperty().bind(control.getMouseY().add(dY));
    }
    @Override public void stop() {
        confirm();
        isSelected.setValue(false);
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
    public void reset(boolean denodify) {
        for (Pin pin : pins) pin.reset(denodify);
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
    protected Rotate getRotation() {
        return rotate;
    }
    protected Scale getScale() {
        return scale;
    }
    protected Pane getRoot() {
        return root;
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
