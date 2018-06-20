package engine.connectivity;

import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.effect.Bloom;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Line;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Wire extends Line implements Connectible {

    // Wire is a line, connecting two points on the circuit grid.
    // For layout convenience, FlyWire class is created.
    // After layout a wire should be confirmed.
    // According to the current connectivity ideology Terilog does
    // not even try parsing connections 'on a fly' - while a circuit
    // is edited by unpredictable user. Therefore, a wire is just a
    // graphical instance until simulation starts. But before the
    // simulation process Terilog does its best and parses the circuit.

    private static final Bloom HIGHLIGHT = new Bloom(0.7);

    private ControlMain control;
    private boolean isFlying;
    private Connection start, end;

    // initialization
    Wire(ControlMain control, IntegerProperty x0, IntegerProperty y0, IntegerProperty x1, IntegerProperty y1) {
        this.control = control;
        isFlying = true;

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
        isFlying = false;

        startXProperty().setValue(Integer.parseInt(w.getAttribute("x0")));
        startYProperty().setValue(Integer.parseInt(w.getAttribute("y0")));
        endXProperty().setValue(Integer.parseInt(w.getAttribute("x1")));
        endYProperty().setValue(Integer.parseInt(w.getAttribute("y1")));
        setStrokeWidth(0.1);
        control.getParent().getChildren().add(this);

        confirm();
    }

    // after layout
    void confirm() {
        if (isFlying) {
            startXProperty().unbind();
            startYProperty().unbind();
            endXProperty().unbind();
            endYProperty().unbind();
        }

        setOpacity(1.0);
        setOnMouseEntered(mouse -> {
            requestFocus();
            setEffect(HIGHLIGHT);
        });
        setOnMouseExited(mouse -> setEffect(null));
        setOnKeyPressed(key -> {
            KeyCode code = key.getCode();
            if (code == KeyCode.DELETE) {
                control.getParent().getChildren().remove(this);
                control.getCircuit().del(this);
            }
        });

        control.getCircuit().add(this);
    }

    // connectivity
    @Override public void connectTo(Connection con) {
        if (con.placeMatches(start)) {
            start = con;
        } else if (con.placeMatches(end)) {
            end = con;
        }
    }
    @Override public void disconnectFrom(Connection con) {
        if (con.placeMatches(start)) start = new Connection(this, start);
        else if (con.placeMatches(end)) end = new Connection(this, end);
    }
    @Override public void totalDisconnect() {
        start.terminate();
        end.terminate();
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

}
