package engine.transistors;

import engine.*;
import engine.interfaces.Mirrorable;
import engine.interfaces.Rotatable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class MOSFET extends Component implements Rotatable, Mirrorable {

    private static final String TAG_VGSTH = "vgsth";
    private static final Color COL_LEGS = Color.rgb(0, 0, 0);
    // pins
    public static final int SOURCE = 0;
    public static final int GATE = 1;
    public static final int DRAIN = 0;

    private int rotation;
    private int mirrorV, mirrorH;

    int vgsth;

    MOSFET(boolean isReal, String uniqueID, double xPos, double yPos, int rotation, boolean mirroredVertical, boolean mirroredHorizontal, int vgsth) {
        super(isReal, uniqueID, xPos, yPos);

        // init values
        this.rotation = rotation;
        if (mirroredVertical) mirrorV = -1;
        else mirrorV = 1;
        if (mirroredHorizontal) mirrorH = -1;
        else mirrorH = 1;
        this.vgsth = vgsth;

        inputs = new Wire[2];
        outputs = new Wire[1];
    }

    abstract void finishRendering(GraphicsContext gc);

    @Override
    public void connect(Wire wire, int type) {
        if (type == AS_INPUT) {
            if (numInp < 2) inputs[(numInp++)-1] = wire;
            else System.err.printf("WARNING: a MOSFET has only two inputs; connection from %s to %s not set.", wire, this);
        } else if (type == AS_OUTPUT) {
            if (numOut == 0) outputs[(numOut++)-1] = wire;
            else System.err.printf("WARNING: a MOSFET has only one output; connection from %s to %s not set.", this, wire);
        }
    }

    // rendering
    @Override
    public void render(GraphicsContext gc) {
        // transform
        gc.translate(x, y);
        gc.rotate(ROTATION_ANGLE * rotation);
        gc.scale(mirrorH, mirrorV);

        // render transistor
        gc.setFill(COL_LEGS);
        // legs:
        gc.strokeLine(-2, -1, +0, -1);
        gc.strokeLine(+1, -1, +2, -1);
        gc.strokeLine(-1, +0, -1, -1);
        gc.strokeLine(+0, +0, -1, -1);
        gc.strokeLine(+1, +0, +1, -1);
        // legs' caps:
        gc.strokeLine(-1.2, 0.0, -0.8, 0.0);
        gc.strokeLine(-0.2, 0.0, +0.2, 0.0);
        gc.strokeLine(+0.8, 0.0, +1.2, 0.0);
        // gate:
        gc.strokeLine(-1.0, 0.2, +1.0, 0.2);
        gc.strokeLine(+0.0, 0.2, +0.0, 1.0);

        finishRendering(gc);

        // transform back
        gc.scale(-mirrorH, -mirrorV);
        gc.rotate(-ROTATION_ANGLE * rotation);
        gc.translate(-x, -y);
    }
    @Override
    public double getWidth() {
        return 4.0;
    }
    @Override
    public double getHeight() {
        return 2.0;
    }

    // rotating
    @Override
    public void rotateClockwise() {
        rotation += 3;
        rotation %= NUM_ROTATIONS;
    }
    @Override
    public void rotateCounterClockwise() {
        rotation += 1;
        rotation %= NUM_ROTATIONS;
    }

    // mirroring
    @Override
    public void mirrorHorizontal() {
        mirrorH *= -1;
    }
    @Override
    public void mirrorVertical() {
        mirrorV *= -1;
    }


    // save/load
    @Override
    public void saveSpecificInfo() {
        TerilogIO.tag(TAG_VGSTH, Integer.toString(vgsth));
    }
    @Override
    public void loadSpecificInfo() {

    }

}
