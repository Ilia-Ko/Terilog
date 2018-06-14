package engine;

import engine.interfaces.Informative;
import engine.interfaces.Mirrorable;
import engine.interfaces.Renderable;
import engine.interfaces.Rotatable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public abstract class Component implements Renderable, Rotatable, Mirrorable, Informative {

    // subclasses   ->   I don't know how to conform OOP in this case! - TODO: rewrite in good OOP style
    protected static final String ATTR_NAME_OF_HARD_N = "hard-n";
    protected static final String ATTR_NAME_OF_HARD_P = "hard-p";
    protected static final String ATTR_NAME_OF_SOFT_N = "soft-n";
    protected static final String ATTR_NAME_OF_SOFT_P = "soft-p";
    protected static final String ATTR_NAME_OF_DIODE = "diode";
    protected static final String ATTR_NAME_OF_RECONCILIATOR = "rec";
    protected static final String ATTR_NAME_OF_VOLTAGE = "volts";
    protected static final String ATTR_NAME_OF_INDICATOR = "ind";

    // rendering
    private double p; // grid period
    private Canvas basis; // canvas for rendering
    private int x, y; // coordinates of the top-left corner of this component
    private double alpha;
    protected int rotation; // index of rotation of this component; see Rotatable
    private int mirrorV, mirrorH; // states of mirroring of this component; see Mirrorable

    // others
    private Pin[] pins;
    private String id;

    protected Component() {
        basis = new Canvas(getWidth(), getHeight());
        x = 0; y = 0;
        rotation = Rotatable.DEFAULT;
        mirrorV = Mirrorable.DEFAULT;
        mirrorH = Mirrorable.DEFAULT;
        pins = initPins();
    }
    protected abstract Pin[] initPins();

    // connectivity
    public abstract Pin getPinByName(String pinName);
    void connect(Node node, Pin pin) {
        pin.connect(node);
        node.addPin(pin);
    }
    void disconnect(Wire wire) {
        Node node = wire.getNode();

        // find the pin
        for (Pin pin : pins)
            if (pin.getNode() == node) {
                pin.disconnect();
                node.delPin(pin);
            }
    }

    // simulation
    public abstract ArrayList<Node> simulate();
    protected abstract boolean isIndependent(); // asks whether this Component has input nodes - 'depends' on them

    // rendering
    @Override public void render() {
        // configure gc
        GraphicsContext gc = basis.getGraphicsContext2D();
        gc.save();
        gc.setGlobalAlpha(alpha);
        gc.scale(p, p);
        gc.clearRect(0, 0, getWidth(), getHeight());

        // render component
        renderBody(gc);

        // reset gc
        gc.restore();
    }
    protected abstract void renderBody(GraphicsContext gc);
    @Override public void setGridPeriod(double period) {
        p = period;
        basis.setWidth(p * getWidth());
        basis.setHeight(p * getHeight());
        basis.setTranslateX(p * x);
        basis.setTranslateY(p * y);
    }
    @Override public void setPos(int xPos, int yPos) {
        for (Pin pin : pins) pin.translate(xPos - x, yPos - y);
        x = xPos;
        y = yPos;
        basis.setTranslateX(p * x);
        basis.setTranslateY(p * y);
    }
    @Override public void setGlobalAlpha(double alpha) {
        this.alpha = alpha;
    }
    @Override public boolean inside(int mx, int my) {
        mx -= x;
        my -= y;
        int w2 = getWidth() / 2;
        int h2 = getHeight() / 2;
        return (mx >= -w2) && (mx <= +w2) && (my >= -h2) && (my <= +h2);
    }
    @Override public Canvas getBasis() {
        return basis;
    }

    // rotating: children should extend these methods
    @Override public void rotateClockwise() {
        rotation += 3;
        rotation %= NUM_ROTATIONS;
    }
    @Override public void rotateCounterClockwise() {
        rotation += 1;
        rotation %= NUM_ROTATIONS;
    }

    // mirroring: children should extend these methods
    @Override public void mirrorHorizontal() {
        mirrorH *= -1;
    }
    @Override public void mirrorVertical() {
        mirrorV *= -1;
    }

    // informative
    @Override public void setID(String id) {
        this.id = id;
    }
    @Override public String getID() {
        return id;
    }
    Pin[] getPins() {
        return pins;
    } // connectivity
    String getX() {
        return Integer.toString(x);
    }
    String getY() {
        return Integer.toString(y);
    }
    String getRotation() {
        return Rotatable.getAttrName(rotation);
    }
    String getMirrorH() {
        return Mirrorable.getAttrName(mirrorH);
    }
    String getMirrorV() {
        return Mirrorable.getAttrName(mirrorV);
    }
    void setRotation(String rotationName) {
        rotation = Rotatable.parseAttrName(rotationName);
    }
    void setMirroring(String mirrorNameH, String mirrorNameV) {
        mirrorH = Mirrorable.parseAttrName(mirrorNameH);
        mirrorV = Mirrorable.parseAttrName(mirrorNameV);
    }
    protected abstract String getAttrClassName();

    protected class Pin {

        // these pin types are relative to the component
        public static final int INPUT = 0;
        public static final int OUTPUT = 1;

        private Component parent;
        private Node node;
        private int x, y;
        private int type;
        private String attrName;

        public Pin(Component parent, int type, String name) {
            x = 0;
            y = 0;
            this.parent = parent;
            this.type = type;
            node = null;
            attrName = name;
        }

        // simulation
        public LogicLevel sig() {
            return node.getCurrentSignal();
        }

        // connectivity
        void connect(Node node) {
            this.node = node;
        }
        void disconnect() {
            node = null;
        }

        // references
        Component getParent() {
            return parent;
        }
        public Node getNode() {
            return node;
        }

        // positioning
        public void setPos(int xPos, int yPos) {
            x = xPos;
            y = yPos;
        }
        private void translate(int dx, int dy) {
            x += dx;
            y += dy;
        }
        int getX() {
            return x;
        }
        int getY() {
            return y;
        }

        // informative
        int getType() {
            return type;
        }
        public String getName() {
            return attrName;
        }

    }

}
