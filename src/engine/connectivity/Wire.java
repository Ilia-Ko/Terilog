package engine.connectivity;

import engine.LogicLevel;
import gui.control.ControlMain;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.effect.Bloom;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Line;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;

public class Wire implements Connectible {

    /* Wire is a polyline (made of line segments - WireLines) that connects two points.
    It is important that polyline is continuous and must not have branches. When a
    branch is going to appear (after user added new wire), the circuit engine must
    split the wire into two short ones so that each wire had no branches.

    It is very convenient to add wires as 'corners' (polyline with two perpendicular segments).
    That's why there are two modes: layout mode and main mode.

    In the layout mode the wire looks like 'corner' and those two elementary lines are very
    important: lineH and lineV. They are following the mouse when layout() is called. A corner
    can be in two states: flipped and straight. Straight corner means that lineH starts at the
    (x0, y0) and lineV ends at (x1, y1). Flipped corner means the opposite. When user ends layout,
    confirm() is called in order to enter main mode.

    In main mode a wire is usually static, but still can change after user adds or removes
    another wire that contacts with this wire.
     */

    private static final Bloom HIGHLIGHT = new Bloom(0.7);

    // layout mode only
    private WireLine lineH, lineV;
    private boolean isFirstH;

    private ControlMain control;
    private ArrayList<WireLine> lines;
    private boolean isTotalSelect;

    public Wire(ControlMain control) {
        this.control = control;
        IntegerProperty mouseX = control.getMouseX();
        IntegerProperty mouseY = control.getMouseY();
        IntegerProperty x0 = new SimpleIntegerProperty(mouseX.get());
        IntegerProperty y0 = new SimpleIntegerProperty(mouseY.get());

        lineH = new WireLine(x0, y0, mouseX, y0);
        lineV = new WireLine(mouseX, y0, mouseX, mouseY);
        isFirstH = true;
    }
    public Wire(ControlMain control, ArrayList<WireLine> lines) { // create a wire in main mode
        this.control = control;
        this.lines = lines;
    }
    public Wire(ControlMain control, Element w) {
        this.control = control;
    }

    // layout mode only
    public void flip() {
        isFirstH = !isFirstH;
        DoubleProperty x, y;
        if (isFirstH) {
            x = lineH.endXProperty();
            y = lineV.startYProperty();
        } else {
            x = lineH.startXProperty();
            y = lineV.endYProperty();
        }
        lineH.startYProperty().bind(y);
        lineH.endYProperty().bind(y);
        lineV.startXProperty().bind(x);
        lineV.endXProperty().bind(x);
    }
    public void confirm() { // enter main mode
        lines = new ArrayList<>();
        lineH.enterMainMode();
        lineV.enterMainMode();
        lineH.connect(lineV);
    }
    public void delete() {
        control.getParent().getChildren().removeAll(lineH, lineV);
        control.getCircuit().del(this);
    }

    // main mode only
    private void totalSelect(boolean isTotalSelect) {
        lines.forEach(line -> line.setEffect(isTotalSelect ? HIGHLIGHT : null));
        this.isTotalSelect = isTotalSelect;
    }
    private void removeSegment(WireLine seg) {
        if (isTotalSelect) // remove the whole wire from the circuit
            control.getCircuit().del(this);
        else { // remove only one segment
            if (seg.isInTheMiddle()) split(seg);
            lines.remove(seg);
            seg.disconnect();
        }
    }

    // connectivity
    public void join(Wire wire) {

    }
    private void split(WireLine splitAt) {
        // find all segments of a branch, connected to the segment (at one side)
        WireLine seed = splitAt.getFirstSegment();
        ArrayList<WireLine> branch = new ArrayList<>();
        findBranch(seed, seed.getNextSegment(), branch);

        lines.removeAll(branch); // remove the branch from this wire
        control.getCircuit().add(new Wire(control, branch)); // but create new wire from the branch
    }
    private static void findBranch(WireLine prev, WireLine curr, ArrayList<WireLine> dstList) {
        dstList.add(curr);
        if (curr.hasNextSegment(prev))
            findBranch(curr, curr.getNextSegment(), dstList);
    }

    @Override
    public void connect(Connectible con) {

    }

    @Override
    public void disconnect(Connectible con) {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public LogicLevel sig() {
        return null;
    }

    @Override
    public void sendSig(LogicLevel signal) {

    }

    // xml info
    public Element writeXML(Document doc) {
        Element w = doc.createElement("wire");
        return w;
    }

    private class WireLine extends Line implements Connectible {

        private WireLine seg1, seg2;
        private Connectible c1, c2;

        private WireLine(IntegerProperty x0, IntegerProperty y0, IntegerProperty x1, IntegerProperty y1) {
            super();
            startXProperty().bind(x0);
            startYProperty().bind(y0);
            endXProperty().bind(x1);
            endYProperty().bind(y1);
            setOpacity(0.4);
            setStrokeWidth(0.1);
            control.getParent().getChildren().add(this);
        }

        private void enterMainMode() {
            startXProperty().unbind();
            startYProperty().unbind();
            endXProperty().unbind();
            endYProperty().unbind();

            lines.add(this);
            setOpacity(1.0);

            setOnMouseEntered(mouse -> {
                requestFocus();
                setEffect(HIGHLIGHT);
            });
            setOnMouseExited(mouse -> {
                if (isTotalSelect)
                    totalSelect(false);
                else
                    setEffect(null);
            });
            setOnKeyPressed(key -> {
                KeyCode code = key.getCode();
                if (code == KeyCode.DELETE)
                    removeSegment(this);
                else if (code == KeyCode.ENTER)
                    totalSelect(true);
            });
        }

        // connectivity
        @Override public void connect(Connectible con) {
            if (c1 == null) {
                c1 = con;
                con.connect(this);
            } else if (c2 == null) {
                c2 = con;
                con.connect(this);
            }
        }
        @Override public void disconnect(Connectible con) {
            if (c1 == con) {
                c1 = null;
                con.disconnect(this);
            } else if (c2 == con) {
                c2 = null;
                con.disconnect(this);
            }
        }
        @Override public void disconnect() {
            c1.disconnect(this);
            c2.disconnect(this);
            c1 = null;
            c2 = null;
        }

        @Override public LogicLevel sig() {
            return null;
        }
        @Override public void sendSig(LogicLevel signal) {

        }

        private boolean hasNextSegment(WireLine exceptFromThis) {
            if (seg1 == exceptFromThis) {
                return seg2 != null;
            } else if (seg2 == exceptFromThis) {
                return seg1 != null;
            } else
                return seg1 == null ^ seg2 == null;
        }
        private boolean isInTheMiddle() {
            return seg1 != null && seg2 != null;
        }
        private WireLine getFirstSegment() {
            c1.disconnect(this);
            return (WireLine) c1;
        }
        private WireLine getNextSegment() {
            if (c1 != null)
                return (WireLine) c1;
            else if (c2 != null)
                return (WireLine) c2;
            else
                return null;
        }

    }

}
