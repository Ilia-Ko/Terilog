package engine.transistors;

import engine.Component;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

@SuppressWarnings("SuspiciousNameCombination")
public abstract class MOSFET extends Component {

    private static final Color COL_LEGS = Color.rgb(0, 0, 0);
    // size
    private static final int WIDTH = 4;
    private static final int HEIGHT = 2;

    // unique for mosfets
    int vgsth;
    private Pin gate, source, drain;

    MOSFET(int vgsth) {
        super();
        this.vgsth = vgsth;
    }
    @Override protected Pin[] initPins() {
        gate = new Pin(this, Pin.INPUT, "gate");
        source = new Pin(this, Pin.INPUT, "source");
        drain = new Pin(this, Pin.OUTPUT, "drain");
        return new Pin[] {gate, source, drain};
    }

    // connectivity
    @Override public Pin getPinByName(String pinName) {
        if (pinName.equals(gate.getName()))
            return gate;
        else if (pinName.equals(source.getName()))
            return source;
        else if (pinName.equals(drain.getName()))
            return drain;
        else
            System.out.printf("WARNING: MOSFETs do not have pin '%s'.\n", pinName);
        return null;
    }

    // simulation
    @Override protected boolean isIndependent() {
        return false;
    }

    // rendering
    abstract void finishRendering(GraphicsContext gc);
    @Override protected void renderBody(GraphicsContext gc) {
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
    @Override public int getWidth() {
        if (rotation % 2 == ROT_RIGHT) return WIDTH;
        else return HEIGHT;
    }
    @Override public int getHeight() {
        if (rotation % 2 == ROT_RIGHT) return HEIGHT;
        else return WIDTH;
    }


}
