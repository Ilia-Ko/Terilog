package engine.transistors;

import engine.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

@SuppressWarnings("SuspiciousNameCombination")
public abstract class MOSFET extends Component {

    private static final String TAG_VGSTH = "vgsth";
    private static final Color COL_LEGS = Color.rgb(0, 0, 0);
    // size
    private static final int WIDTH = 4;
    private static final int HEIGHT = 2;
    // pins
    public static final int SOURCE = 0;
    public static final int GATE   = 1;
    public static final int DRAIN  = 2;

    int vgsth;

    MOSFET(int vgsth) {
        super();
        this.vgsth = vgsth;
        inputs = new Wire[2];
        outputs = new Wire[1];
    }

    // rendering
    abstract void finishRendering(GraphicsContext gc);
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
        gc.scale(mirrorH, mirrorV);
        gc.rotate(-ROTATION_ANGLE * rotation);
        gc.translate(-x, -y);
    }
    @Override
    public int getWidth() {
        if (rotation % 2 == ROT_RIGHT) return WIDTH;
        else return HEIGHT;
    }
    @Override
    public int getHeight() {
        if (rotation % 2 == ROT_RIGHT) return HEIGHT;
        else return WIDTH;
    }

    // connecting
    @Override
    public void connect(Wire wire, int type) {

    }
    @Override
    public void disconnect(Wire unit) {

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
