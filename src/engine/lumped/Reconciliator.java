package engine.lumped;

import engine.Component;
import engine.Node;
import gui.control.ControlMain;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Reconciliator extends Component {

    public static final String ATTR_CLASS_NAME = "rec";
    private static final int SIZE = 2;

    // pins
    private Pin source, pull, drain;

    // initialization
    @Override protected Pin[] initPins() {
        source = new Pin(this, "source");
        pull = new Pin(this, "pull");
        drain = new Pin(this, "drain");

        source.setPos(0, 1);
        pull.setPos(1, 0);
        drain.setPos(2, 1);

        return new Pin[] {source, pull, drain};
    }
    @Override public Component newCompOfTheSameClass() {
        return new Reconciliator();
    }

    // simulation
    @Override
    public ArrayList<Node> simulate() {
        // compute signals
        if (source.sig().isUnstable())
            drain.sendSig(pull.sig());
        else
            drain.sendSig(source.sig());

        // affected nodes
        ArrayList<Node> affected = new ArrayList<>();
        affected.add(drain.getNode());
        return affected;
    }
    @Override protected boolean isIndependent() {
        return false;
    }

    // rendering
    @Override protected void renderBody(GraphicsContext gc) {
        gc.setLineWidth(ControlMain.LINE_WIDTH);
        gc.setStroke(Color.BLACK);

        // pin legs
        gc.strokeLine(0, 1, 0.5, 1);
        gc.strokeLine(1, 0, 1, 0.5);
        gc.strokeLine(1.5, 1, 2, 1);

        // body
        gc.strokeOval(0.5, 0.5, 1, 1);
    }
    @Override protected int getAbsoluteWidth() {
        return SIZE;
    }
    @Override protected int getAbsoluteHeight() {
        return SIZE;
    }

    // informative
    @Override public String toString() {
        return "Reconciliator";
    }
    @Override protected String getAttrClassName() {
        return ATTR_CLASS_NAME;
    }
    @Override public String getPrefixID() {
        return "r";
    }

}
