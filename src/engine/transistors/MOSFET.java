package engine.transistors;

import engine.Component;
import gui.control.ControlMain;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

@SuppressWarnings("SuspiciousNameCombination")
public abstract class MOSFET extends Component {

    // size
    private static final int WIDTH = 4;
    private static final int HEIGHT = 2;

    // pins
    Pin gate, source, drain;

    // initialization
    @Override protected Pin[] initPins() {
        // create pins
        gate = new Pin(this, Pin.INPUT, "gate");
        source = new Pin(this, Pin.INPUT, "source");
        drain = new Pin(this, Pin.OUTPUT, "drain");

        // place them
        gate.setPos(2, 0);
        source.setPos(0, 2);
        drain.setPos(4, 2);

        return new Pin[] {gate, source, drain};
    }

    // simulation
    @Override protected boolean isIndependent() {
        return false;
    }

    // rendering
    @Override protected void renderBody(GraphicsContext gc) {
        gc.setStroke(getLegsColour());
        gc.setLineWidth(ControlMain.LINE_WIDTH);

        // legs:
        gc.strokeLine(0, 2, 2, 2);
        gc.strokeLine(3, 2, 4, 2);
        gc.strokeLine(1, 1, 1, 2);
        gc.strokeLine(2, 1, 2, 2);
        gc.strokeLine(3, 1, 3, 2);

        // legs' caps:
        double a = 0.2;
        gc.strokeLine(1-a, 1, 1+a, 1);
        gc.strokeLine(2-a, 1, 2+a, 1);
        gc.strokeLine(3-a, 1, 3+a, 1);

        // gate:
        gc.strokeLine(1, 1-a, 3, 1-a);
        gc.strokeLine(2, 1-a, 2, 0);

        // MOSFET needs a triangle and sometimes a circle for N-channel and P-channel FETs
        renderSpecific(gc);
    }
    abstract void renderSpecific(GraphicsContext gc);
    abstract Color getLegsColour();
    @Override public int getWidth() {
        if (rotation % 2 == ROT_RIGHT) return WIDTH;
        else return HEIGHT;
    }
    @Override public int getHeight() {
        if (rotation % 2 == ROT_RIGHT) return HEIGHT;
        else return WIDTH;
    }
    @Override protected int getAbsoluteWidth() {
        return WIDTH;
    }
    @Override protected int getAbsoluteHeight() {
        return HEIGHT;
    }

}
