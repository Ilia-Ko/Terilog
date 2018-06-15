package engine;

import engine.interfaces.Informative;
import engine.interfaces.Mirrorable;
import engine.interfaces.Renderable;
import engine.interfaces.Rotatable;
import gui.control.ControlMain;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;

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
        basis = new Canvas();
        x = 0; y = 0;
        rotation = Rotatable.DEFAULT;
        mirrorV = Mirrorable.DEFAULT;
        mirrorH = Mirrorable.DEFAULT;
        pins = initPins();
    }
    protected abstract Pin[] initPins();
    public void initContextMenu(ControlMain control) {
        ContextMenu menu = control.makeContextMenuFor(this);
        basis.setOnContextMenuRequested(event -> menu.show(basis, event.getScreenX(), event.getScreenY()));
    }
    public abstract Component newCompOfTheSameClass();

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
                node.delPin(pin);
                pin.disconnect();
            }
    }
    void disconnect() {
        for (Pin pin : pins) {
            pin.getNode().delPin(pin);
            pin.disconnect();
        }
    }

    // simulation
    public abstract ArrayList<Node> simulate();
    protected abstract boolean isIndependent(); // asks whether this Component has input nodes - 'depends' on them

    // rendering
    @Override public void render() {
        double w = getWidth() + Pin.PIN_CIRCLE_RADIUS * 2;
        double h = getHeight() + Pin.PIN_CIRCLE_RADIUS * 2;
        double aw2 = -getAbsoluteWidth() / 2;
        double ah2 = -getAbsoluteHeight() / 2;

        // configure gc
        GraphicsContext gc = basis.getGraphicsContext2D();
        gc.save();
        gc.setGlobalAlpha(alpha);
        gc.scale(p, p);
        gc.clearRect(0, 0, w, h);

        // render pins
        for (Pin pin : pins) pin.render(gc);

        // perform mirroring
        gc.scale(mirrorH, mirrorV);
        double tx = w * (NOT_MIRRORED - mirrorH) * 0.5;
        double ty = h * (NOT_MIRRORED - mirrorV) * 0.5;
        gc.translate(-tx, -ty);
        // perform rotation
        gc.translate(w / 2, h / 2);
        gc.rotate(-ROTATION_ANGLE * rotation);
        gc.translate(aw2, ah2);

        // render component
        renderBody(gc);

        // reset gc
        gc.restore();
    }
    protected abstract void renderBody(GraphicsContext gc);
    protected abstract int getAbsoluteWidth(); // in periods, width not affected by rotation
    protected abstract int getAbsoluteHeight(); // in periods, height not affected by rotation
    @Override public void setGridPeriod(double period) {
        p = period;
        basis.setWidth(p * (getWidth() + Pin.PIN_CIRCLE_RADIUS * 2));
        basis.setHeight(p * (getHeight() + Pin.PIN_CIRCLE_RADIUS * 2));
        basis.setTranslateX(p * (x - Pin.PIN_CIRCLE_RADIUS));
        basis.setTranslateY(p * (y - Pin.PIN_CIRCLE_RADIUS));
    }
    @Override public void setPos(int xPos, int yPos) {
        for (Pin pin : pins) pin.translate(xPos - x, yPos - y);
        x = xPos;
        y = yPos;
        basis.setTranslateX(p * (x - Pin.PIN_CIRCLE_RADIUS));
        basis.setTranslateY(p * (y - Pin.PIN_CIRCLE_RADIUS));
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

    // rotating
    @Override public void rotateCW() {
        // rotate pins
        for (Pin pin : pins) pin.rotateCW();

        // compute new rotation
        rotation += 3;
        rotation %= NUM_ROTATIONS;

        // adjust canvas size
        basis.setWidth((getWidth() + Pin.PIN_CIRCLE_RADIUS * 2) * p);
        basis.setHeight((getHeight() + Pin.PIN_CIRCLE_RADIUS * 2) * p);

        render();
    }
    @Override public void rotateCCW() {
        // rotate pins
        for (Pin pin : pins) pin.rotateCCW();

        // compute new rotation
        rotation += 1;
        rotation %= NUM_ROTATIONS;

        // adjust canvas size
        basis.setWidth((getWidth() + Pin.PIN_CIRCLE_RADIUS * 2) * p);
        basis.setHeight((getHeight() + Pin.PIN_CIRCLE_RADIUS * 2) * p);

        render();
    }

    // mirroring
    @Override public void mirrorHorizontal() {
        // mirror pins
        for (Pin pin : pins) pin.mirrorHorizontal();

        // mirror
        mirrorH *= -1;

        render();
    }
    @Override public void mirrorVertical() {
        // mirror pins
        for (Pin pin : pins) pin.mirrorVertical();

        // mirror
        mirrorV *= -1;

        render();
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

    protected class Pin implements Rotatable, Mirrorable {

        private static final double PIN_CIRCLE_RADIUS = ControlMain.GRID_POINT_RADIUS * 4;

        // these pin types are relative to the component
        public static final int INPUT = 0;
        public static final int OUTPUT = 1;

        private Component parent;
        private Node node;
        private int x, y; // global coordinates (absolute values in periods)
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
            if (node != null)
                return node.getCurrentSignal();
            else
                return LogicLevel.ZZZ;
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

        // positioning and rendering
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
        private void render(GraphicsContext gc) {
            gc.setFill(sig().colour());

            double b = ControlMain.LINE_WIDTH;
            double c = PIN_CIRCLE_RADIUS * 2 - b;
            double px = x - parent.x;
            double py = y - parent.y;
            gc.fillOval(px + b / 2, py + b / 2, c, c);
        }

        // rotating
        @Override public void rotateCW() {
            // get relative coordinates in parent
            int px = x - parent.x;
            int py = y - parent.y;

            // rotate clockwise
            x = parent.x + parent.getHeight() - py;
            y = parent.y + px;
        }
        @Override public void rotateCCW() {
            // get relative coordinates in parent
            int px = x - parent.x;
            int py = y - parent.y;

            // rotate counterclockwise
            x = parent.x + py;
            y = parent.y + parent.getWidth() - px;
        }

        // mirroring
        @Override public void mirrorHorizontal() {
            // get relative x-coordinate in parent
            int px = x - parent.x;

            // mirror horizontal
            x = parent.x + parent.getWidth() - px;
        }
        @Override public void mirrorVertical() {
            // get relative y-coordinate in parent
            int py = y - parent.y;

            // mirror horizontal
            y = parent.y + parent.getHeight() - py;
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
