package engine;

import engine.interfaces.Informative;
import engine.interfaces.Mirrorable;
import engine.interfaces.Renderable;
import engine.interfaces.Rotatable;
import gui.Control;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public abstract class Component implements Renderable, Rotatable, Mirrorable, Informative {

    // subclasses   ->   I don't know how to conform OOP in this case! - TODO: rewrite in OOP style
    static final String ATTR_NAME_OF_HARD_N = "hard-n";
    static final String ATTR_NAME_OF_HARD_P = "hard-p";
    static final String ATTR_NAME_OF_SOFT_N = "soft-n";
    static final String ATTR_NAME_OF_SOFT_P = "soft-p";
    static final String ATTR_NAME_OF_DIODE = "diode";
    static final String ATTR_NAME_OF_RECONCILIATOR = "rec";
    static final String ATTR_NAME_OF_VOLTAGE = "volts";
    static final String ATTR_NAME_OF_INDICATOR = "ind";

    // rendering
    protected int x, y; // coordinates of the center of this component
    protected int rotation; // index of rotation of this component; see Rotatable
    protected int mirrorV, mirrorH; // states of mirroring of this component; see Mirrorable

    // others
    private Pin[] pins;
    private String id;

    protected Component() {
        x = 0;
        y = 0;
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
    protected abstract void renderBody(GraphicsContext gc);
    @Override public void render(GraphicsContext gc) {
        // render pins
        for (Pin pin : pins) pin.render(gc);
        // render component
        renderBody(gc);
    }
    @Override public void setPos(int xPos, int yPos) {
        x = xPos;
        y = yPos;
        for (Pin pin : pins) pin.setPos(x, y);
    }
    @Override public boolean inside(int mx, int my) {
        mx -= x;
        my -= y;
        int w2 = getWidth() / 2;
        int h2 = getHeight() / 2;
        return (mx >= -w2) && (mx <= +w2) && (my >= -h2) && (my <= +h2);
    }

    // rotating
    @Override public void rotateClockwise() {
        rotation += 3;
        rotation %= NUM_ROTATIONS;
    }
    @Override public void rotateCounterClockwise() {
        rotation += 1;
        rotation %= NUM_ROTATIONS;
    }

    // mirroring
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
    abstract String getAttrClassName();

    protected class Pin {

        // these pin types are relative to the component
        public static final int INPUT = 0;
        public static final int OUTPUT = 1;

        private Component parent;
        private Node node;
        private int x, y; // relative to parent component
        private int type;
        private boolean isConnected;
        private String attrName;

        public Pin(Component parent, int type, String name) {
            x = 0;
            y = 0;
            this.parent = parent;
            this.type = type;
            node = null;
            isConnected = false;
            attrName = name;
        }

        // connectivity
        void connect(Node node) {
            this.node = node;
        }
        void disconnect() {
            isConnected = false;
            node = null;
        }

        // references
        Component getParent() {
            return parent;
        }
        Node getNode() {
            return node;
        }

        // rendering
        private void render(GraphicsContext gc) {
            double r = Control.LINE_WIDTH * 2.0;
            // configure gc
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(Control.LINE_WIDTH);
            if (isConnected)
                gc.setFill(node.getCurrentSignal().colour());
            else
                gc.setFill(Color.TRANSPARENT);

            // render
            gc.strokeOval(x, y, r, r);
            gc.fillOval(x, y, r, r);
        }
        private void setPos(int xPos, int yPos) {
            x = xPos;
            y = yPos;
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
