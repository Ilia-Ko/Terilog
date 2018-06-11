package engine;

import engine.interfaces.Informative;
import engine.interfaces.Mirrorable;
import engine.interfaces.Renderable;
import engine.interfaces.Rotatable;

import java.util.ArrayList;

public abstract class Component implements Renderable, Rotatable, Mirrorable, Informative {

    // rendering
    protected Pin[] pins;
    protected int x, y; // coordinates of the center of this component
    protected int rotation; // index of rotation of this component; see Rotatable
    protected int mirrorV, mirrorH; // states of mirroring of this component; see Mirrorable

    // terilog
    protected Node[] inputs, outputs; // input and output nodes
    protected int numInp, numOut; // their count, the same as <array>.length
    private String id;

    protected Component() {
        x = 0;
        y = 0;
        rotation = Rotatable.DEFAULT;
        mirrorV = Mirrorable.DEFAULT;
        mirrorH = Mirrorable.DEFAULT;
        numInp = 0;
        numOut = 0;
    }

    // connectivity
    public abstract void connect(Node node, Pin pin);
    public abstract void disconnect(Node node, Pin pin);

    // simulation
    public abstract ArrayList<Node> simulate();
    public abstract boolean isIndependent(); // asks whether this Component has input nodes - 'depends' on them

    // rendering
    @Override public void setPos(int xPos, int yPos) {
        x = xPos;
        y = yPos;
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
    @Override public String getX() {
        return Integer.toString(x);
    }
    @Override public String getY() {
        return Integer.toString(y);
    }
    public Pin[] getPins() {
        return pins;
    } // connectivity
    public abstract String getClassName();
    public abstract String getPinName(Pin pin);
    public String getRotation() {
        return Rotatable.getName(rotation);
    }
    public String getMirrorH() {
        return Integer.toString(mirrorH);
    }
    public String getMirrorV() {
        return Integer.toString(mirrorV);
    }

    public class Pin {

        // these pin types are relative to the component
        public static final int INPUT = 0;
        public static final int OUTPUT = 1;

        private Component parent;
        private Wire wire;
        private int x, y;
        private int type;
        private boolean isConnected;

        private Pin(Component parent, int type) {
            x = 0;
            y = 0;
            this.type = type;
            isConnected = false;
        }

        public int getType() {
            return type;
        }
        public Component getParent() {
            return parent;
        }
        public void bindWithWire(Wire w) {
            wire = w;
        }
        public Node getNode() {
            return wire.getNode();
        }

        private void render(GraphicsContext gc) {

        }
        private void setPos(int xPos, int yPos) {
            x = xPos;
            y = yPos;
        }
        public int getX() {
            return x;
        }
        public int getY() {
            return y;
        }

    }

}
