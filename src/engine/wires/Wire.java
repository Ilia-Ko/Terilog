package engine.wires;

import engine.LogicLevel;
import engine.connectivity.Connectible;
import engine.connectivity.Node;
import engine.connectivity.Selectable;
import gui.control.ControlMain;
import gui.control.HistoricalEvent;
import javafx.beans.property.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Wire extends Line implements Connectible, Selectable {

    // Wire is a line, connecting two points on the circuit grid.
    // For layout convenience, FlyWire class is created.
    // After layout a wire should be confirmed.
    // According to the current connectivity ideology Terilog does
    // not even try parsing connections 'on a fly' - while a circuit
    // is edited by unpredictable user. Therefore, a wire is just a
    // graphical instance until simulation starts. But before the
    // simulation process Terilog does its best and parses the circuit.

    private Rectangle r1, r2;
    private ControlMain control;
    private HashSet<Connectible> connectibles;
    private Node node;
    private BooleanProperty isSelected;
    private HistoricalEvent toBeOrNotToBe;

    // initialization
    Wire(ControlMain control, IntegerProperty x0, IntegerProperty y0, IntegerProperty x1, IntegerProperty y1) {
        this.control = control;
        isSelected = new SimpleBooleanProperty(false);
        isSelected.addListener((observable, wasSelected, nowSelected) -> setEffect(nowSelected ? HIGHLIGHT : null));

        // create wire in layout mode
        startXProperty().bind(x0);
        startYProperty().bind(y0);
        endXProperty().bind(x1);
        endYProperty().bind(y1);
        setOpacity(0.4);
        setStrokeWidth(0.1);
        control.getParent().getChildren().add(this);
    }
    public Wire(ControlMain control, Element w) {
        this.control = control;
        isSelected = new SimpleBooleanProperty(false);
        isSelected.addListener((observable, wasSelected, nowSelected) -> setEffect(nowSelected ? HIGHLIGHT : null));

        startXProperty().setValue(getInt(w, "x0"));
        startYProperty().setValue(getInt(w, "y0"));
        endXProperty().setValue(getInt(w, "x1"));
        endYProperty().setValue(getInt(w, "y1"));
        setStrokeWidth(0.1);
        control.getParent().getChildren().add(this);

        confirm();
    }
    private ContextMenu buildContextMenu() {
        MenuItem itemDel = new MenuItem("Remove");
        itemDel.setOnAction(action -> {
            control.rewriteHistory(toBeOrNotToBe);
            delete(true);
            control.appendHistory(HistoricalEvent.invert(toBeOrNotToBe));
        });

        return new ContextMenu(itemDel);
    }

    // layout mode
    void confirm() {
        startXProperty().unbind();
        startYProperty().unbind();
        endXProperty().unbind();
        endYProperty().unbind();

        ContextMenu menu = buildContextMenu();
        setOnContextMenuRequested(mouse -> {
            if (isSelected.not().get())
                menu.show(this, mouse.getScreenX(), mouse.getScreenY());
        });

        setOpacity(1.0);
        control.getCircuit().add(this);

        // add squares
        double a = 1.0 / 6.0, b = a * 2.0;
        r1 = new Rectangle(getStartX() - a, getStartY() - a, b, b);
        r1.fillProperty().bind(strokeProperty());
        r1.setOnContextMenuRequested(mouse -> menu.show(this, mouse.getScreenX(), mouse.getScreenY()));
        r2 = new Rectangle(getEndX() - a, getEndY() - a, b, b);
        r2.fillProperty().bind(strokeProperty());
        r2.setOnContextMenuRequested(mouse -> menu.show(this, mouse.getScreenX(), mouse.getScreenY()));
        control.getParent().getChildren().addAll(r1, r2);

        // history
        final Wire me = this;
        toBeOrNotToBe = new HistoricalEvent() {
            @Override public void undo() {
                control.getParent().getChildren().removeAll(me, r1, r2);
                control.getCircuit().del(me);
            }
            @Override public void redo() {
                control.getParent().getChildren().addAll(me, r1, r2);
                control.getCircuit().add(me);
            }
        };
        control.appendHistory(toBeOrNotToBe);
    }
    public void delete(boolean fromCircuit) {
        if (fromCircuit) control.getCircuit().del(this);
        control.getParent().getChildren().removeAll(this, r1, r2);
    }

    // selection
    @Override public boolean checkSelection(Rectangle sel) {
        isSelected.setValue(sel.intersects(getBoundsInParent()));
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
        setOpacity(0.4);

        // move
        control.getParent().getChildren().removeAll(r1, r2);
        IntegerProperty dStartX = new SimpleIntegerProperty(startXProperty().intValue() - control.getMouseX().get());
        IntegerProperty dStartY = new SimpleIntegerProperty(startYProperty().intValue() - control.getMouseY().get());
        IntegerProperty dEndX = new SimpleIntegerProperty(endXProperty().intValue() - control.getMouseX().get());
        IntegerProperty dEndY = new SimpleIntegerProperty(endYProperty().intValue() - control.getMouseY().get());
        startXProperty().bind(control.getMouseX().add(dStartX));
        startYProperty().bind(control.getMouseY().add(dStartY));
        endXProperty().bind(control.getMouseX().add(dEndX));
        endYProperty().bind(control.getMouseY().add(dEndY));
    }
    @Override public void stop() {
        confirm();
        isSelected.setValue(false);
    }

    // connectivity
    @Override public void reset(boolean denodify) {
        if (denodify) {
            node = null;
            connectibles = new HashSet<>();
        }
        put(LogicLevel.ZZZ);
    }
    @Override public void put(LogicLevel signal) {
        setStroke(signal.colour());
    }
    @Override public LogicLevel get() {
        return null;
    }
    // parsing.stage1
    @Override public void inspect(Wire wire) {
        int x0, y0, x1, y1;

        // check if this wire touches given one
        x0 = this.startXProperty().intValue();
        y0 = this.startYProperty().intValue();
        x1 = this.endXProperty().intValue();
        y1 = this.endYProperty().intValue();
        boolean a = wire.inside(x0, y0) || wire.inside(x1, y1);

        // check if given wire touches this one
        x0 = wire.startXProperty().intValue();
        y0 = wire.startYProperty().intValue();
        x1 = wire.endXProperty().intValue();
        y1 = wire.endYProperty().intValue();
        boolean b = this.inside(x0, y0) || this.inside(x1, y1);

        if (a || b) Connectible.establishConnection(this, wire);
    }
    @Override public boolean inside(int x, int y) {
        int x0 = startXProperty().intValue();
        int y0 = startYProperty().intValue();
        int x1 = endXProperty().intValue();
        int y1 = endYProperty().intValue();

        if (x0 == x1) return x == x0 && between(y, y0, y1);
        else if (y0 == y1) return y == y0 && between(x, x0, x1);
        else {
            // diagonal wires are not recommended, but it is not forbidden
            double k = (double) (y1 - y0) / (double) (x1 - x0);
            double m = y0 - k * x0;
            return y == (int) Math.round(k * x + m);
        }
    }
    @Override public void connect(Connectible con) {
        connectibles.add(con);
    }
    // parsing.stage2
    @Override public boolean isNodeFree() {
        return node == null;
    }
    @Override public void nodify(Node node) {
        node.add(this);
        this.node = node;
        for (Connectible con : connectibles)
            if (con.isNodeFree()) con.nodify(node);
    }

    // xml info
    public Element writeXML(Document doc) {
        Element w = doc.createElement("wire");
        w.setAttribute("x0", asInt(startXProperty()));
        w.setAttribute("y0", asInt(startYProperty()));
        w.setAttribute("x1", asInt(endXProperty()));
        w.setAttribute("y1", asInt(endYProperty()));
        return w;
    }

    // util
    private static String asInt(DoubleProperty d) {
        return Integer.toString(d.intValue());
    }
    private static int getInt(Element from, String what) {
        return Integer.parseInt(from.getAttribute(what));
    }
    private static boolean between(int what, int a, int b) {
        return Math.abs(what - a) + Math.abs(what - b) == Math.abs(a - b);
    }

}
