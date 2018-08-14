package engine.wires;

import engine.LogicLevel;
import engine.connectivity.Connectible;
import engine.connectivity.Node;
import engine.connectivity.Selectable;
import gui.control.ControlMain;
import gui.control.HistoricalEvent;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.HashSet;

import static engine.LogicLevel.ZZZ;

public class Wire extends Region implements Connectible, Selectable {

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
    // layout
    IntegerProperty x0, y0, x1, y1;
    private int length;
    private Line[] lines;

    // initialization
    private Wire(ControlMain control, int busLength) {
        this.control = control;
        length = busLength;
        x0 = new SimpleIntegerProperty();
        y0 = new SimpleIntegerProperty();
        x1 = new SimpleIntegerProperty();
        y1 = new SimpleIntegerProperty();
        isSelected = new SimpleBooleanProperty(false);
        isSelected.addListener((observable, wasSelected, nowSelected) -> setEffect(nowSelected ? HIGHLIGHT : null));
        
        // create wire lines
        DoubleProperty shift = new SimpleDoubleProperty(-0.1 * (length - 1) / 2.0);
        DoubleBinding ex = (DoubleBinding) x1.subtract(x0).add(shift);
        DoubleBinding ey = (DoubleBinding) y1.subtract(y0).add(shift);
        lines = new Line[length];
        for (int i = 0; i < lines.length; i++) {
            double delta = i * 0.1;
            Line line = new Line();
            line.startXProperty().bind(shift.add(delta));
            line.startYProperty().bind(shift.add(delta));
            line.endXProperty().bind(ex.add(delta));
            line.endYProperty().bind(ey.add(delta));
            line.setStrokeWidth(0.08);
            line.setStroke(ZZZ.colour());
            lines[i] = line;
        }
        getChildren().addAll(lines);

        // place them
        layoutXProperty().bind(x0);
        layoutYProperty().bind(y0);
        control.getParent().getChildren().addAll(this);
    }
    Wire(ControlMain control, int busLength, IntegerProperty ix0, IntegerProperty iy0, IntegerProperty ix1, IntegerProperty iy1) {
        this(control, busLength);
        x0.bind(ix0);
        y0.bind(iy0);
        x1.bind(ix1);
        y1.bind(iy1);
        setOpacity(0.4);
    }
    public Wire(ControlMain control, int busLength, Element w) {
        this(control, busLength);
        x0.setValue(getInt(w, "x0"));
        y0.setValue(getInt(w, "y0"));
        x1.setValue(getInt(w, "x1"));
        y1.setValue(getInt(w, "y1"));
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
    public void confirm() {
        x0.unbind();
        y0.unbind();
        x1.unbind();
        y1.unbind();

        ContextMenu menu = buildContextMenu();
        setOpacity(1.0);
        control.getCircuit().add(this);

        // add squares
        double b = 1.0 / 3.0 + (length - 1) * 0.1;
        DoubleProperty a = new SimpleDoubleProperty(b / 2.0);
        r1 = new Rectangle(b, b);
        r1.xProperty().bind(x0.subtract(a));
        r1.yProperty().bind(y0.subtract(a));
        r1.setArcWidth(0.3);
        r1.setArcHeight(0.3);
        r1.setFill(Color.BLACK);
        r1.setOpacity(0.8);
        r1.setOnContextMenuRequested(mouse -> menu.show(this, mouse.getScreenX(), mouse.getScreenY()));
        r2 = new Rectangle(b, b);
        r2.xProperty().bind(x1.subtract(a));
        r2.yProperty().bind(y1.subtract(a));
        r2.setArcWidth(0.3);
        r2.setArcHeight(0.3);
        r2.setFill(Color.BLACK);
        r2.setOpacity(0.8);
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
        isSelected.setValue(sel.getBoundsInParent().contains(getBoundsInParent()));
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
        IntegerProperty dStartX = new SimpleIntegerProperty(x0.intValue() - control.getMouseX().get());
        IntegerProperty dStartY = new SimpleIntegerProperty(y0.intValue() - control.getMouseY().get());
        IntegerProperty dEndX = new SimpleIntegerProperty(x1.intValue() - control.getMouseX().get());
        IntegerProperty dEndY = new SimpleIntegerProperty(y1.intValue() - control.getMouseY().get());
        x0.bind(control.getMouseX().add(dStartX));
        y0.bind(control.getMouseY().add(dStartY));
        x1.bind(control.getMouseX().add(dEndX));
        y1.bind(control.getMouseY().add(dEndY));
    }
    @Override public Selectable copy() {
        IntegerProperty ix0 = new SimpleIntegerProperty(x0.intValue());
        IntegerProperty iy0 = new SimpleIntegerProperty(y0.intValue());
        IntegerProperty ix1 = new SimpleIntegerProperty(x1.intValue());
        IntegerProperty iy1 = new SimpleIntegerProperty(y1.intValue());

        Wire copy = new Wire(control, length, ix0, iy0, ix1, iy1);
        copy.stop();
        return copy;
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
        for (Line line : lines) line.setStroke(ZZZ.colour());
    }
    @Override public void put(LogicLevel[] signal) {
        assert signal.length == length;
        for (int i = 0; i < length; i++) lines[i].setStroke(signal[i].colour());
    }
    @Override public LogicLevel[] get() {
        return null;
    }
    @Override public int capacity() {
        return length;
    }

    // parsing.stage1
    @Override public void inspect(Wire wire) {
        int x0, y0, x1, y1;

        // check if this wire touches given one
        x0 = this.x0.intValue();
        y0 = this.y0.intValue();
        x1 = this.x1.intValue();
        y1 = this.y1.intValue();
        boolean a = wire.inside(x0, y0) || wire.inside(x1, y1);

        // check if given wire touches this one
        x0 = wire.x0.intValue();
        y0 = wire.y0.intValue();
        x1 = wire.x1.intValue();
        y1 = wire.y1.intValue();
        boolean b = this.inside(x0, y0) || this.inside(x1, y1);

        if (a || b) Connectible.establishConnection(this, wire);
    }
    @Override public boolean inside(int x, int y) {
        int x0 = this.x0.intValue();
        int y0 = this.y0.intValue();
        int x1 = this.x1.intValue();
        int y1 = this.y1.intValue();

        if (x0 == x1) return x == x0 && between(y, y0, y1);
        else if (y0 == y1) return y == y0 && between(x, x0, x1);
        else {
            // diagonal wires are neither recommended nor forbidden
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
        w.setAttribute("bus", Integer.toString(length));
        w.setAttribute("x0", asInt(x0));
        w.setAttribute("y0", asInt(y0));
        w.setAttribute("x1", asInt(x1));
        w.setAttribute("y1", asInt(y1));
        return w;
    }

    // util
    private static String asInt(IntegerProperty i) {
        return Integer.toString(i.intValue());
    }
    private static int getInt(Element from, String what) {
        return Integer.parseInt(from.getAttribute(what));
    }
    private static boolean between(int what, int a, int b) {
        return Math.abs(what - a) + Math.abs(what - b) == Math.abs(a - b);
    }
    public static Wire optimize(Wire w1, Wire w2) {
        assert w1.capacity() == w2.capacity();
        
        // intersecting
        boolean s2in1 = w1.inside(w2.x0.intValue(), w2.y0.intValue());
        boolean e2in1 = w1.inside(w2.x1.intValue(), w2.y1.intValue());
        boolean s1in2 = w2.inside(w1.x0.intValue(), w1.y0.intValue());
        boolean e1in2 = w2.inside(w1.x1.intValue(), w1.y1.intValue());
        // vertically parallel
        boolean vPar = w1.x0.intValue() == w1.x1.intValue() &&
                w1.x1.intValue() == w2.x0.intValue() &&
                w2.x0.intValue() == w2.x1.intValue();
        // horizontally parallel
        boolean hPar = w1.y0.intValue() == w1.y1.intValue() &&
                w1.y1.intValue() == w2.y0.intValue() &&
                w2.y0.intValue() == w2.y1.intValue();

        if ((s2in1 || e2in1) && (hPar || vPar)) {
            if (s2in1 && e2in1) { // w1 covers w2 completely
                return w2;
            } else if (s1in2 && e1in2) { // w2 covers w1 completely
                return w1;
            } else if (s2in1 && e1in2) { // w1 overlaps w2
                if (hPar) w1.x1.setValue(w2.x1.intValue());
                else w1.y1.setValue(w2.y1.intValue());
                return w2;
            } else if (s1in2 && e2in1) { // w2 overlaps w1
                if (hPar) w2.x1.setValue(w1.x1.intValue());
                else w2.y1.setValue(w1.y1.intValue());
                return w1;
            } else if (s2in1 && s1in2) { // common start
                if (hPar) w1.x0.setValue(w2.x1.intValue());
                else w1.y0.setValue(w2.y1.intValue());
                return w2;
            } else if (e2in1&& e1in2) { // common end
                if (hPar) w1.x1.setValue(w2.x0.intValue());
                else w1.y1.setValue(w2.y0.intValue());
                return w2;
            }
        }
        return null;
    }

}
