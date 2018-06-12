package engine.transistors;

import engine.Component;
import engine.LogicLevel;
import engine.Node;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class HardP extends MOSFET {

    private static final int VGSTH = -2;

    // simulation
    @Override public ArrayList<Node> simulate() {
        // compute output signal
        LogicLevel out;
        if (gate.sig() == LogicLevel.ZZZ && source.sig() == LogicLevel.ZZZ)
            out = LogicLevel.ZZZ;
        else if (!gate.sig().stable() || !source.sig().stable())
            out = LogicLevel.ERR;
        else if (gate.sig().volts() - source.sig().volts() <= VGSTH)
            out = source.sig();
        else
            out = LogicLevel.ZZZ;

        // influence on the output node
        drain.getNode().receiveSignal(this, out);

        // report about affected nodes
        ArrayList<Node> affected = new ArrayList<>();
        affected.add(drain.getNode());
        return affected;
    }

    // rendering
    @Override void finishRendering(GraphicsContext gc) {
        gc.fillPolygon(new double[] {-0.2, +0.2,  0.0},
                       new double[] {-0.2, -0.2, -0.4}, 3);
    }

    // informative
    @Override public String getPrefixID() {
        return "hp";
    }
    @Override protected String getAttrClassName() {
        return Component.ATTR_NAME_OF_HARD_P;
    }

}
