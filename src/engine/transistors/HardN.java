package engine.transistors;

import engine.Component;
import engine.LogicLevel;
import engine.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class HardN extends MOSFET {

    private static final Color COL_LEGS = Color.DARKBLUE;
    private static final int VGSTH = 2;

    // simulation
    @Override public ArrayList<Node> simulate() {
        // compute output signal
        LogicLevel out;
        if (gate.sig() == LogicLevel.ZZZ && source.sig() == LogicLevel.ZZZ)
            out = LogicLevel.ZZZ;
        else if (!gate.sig().stable() || !source.sig().stable())
            out = LogicLevel.ERR;
        else if (gate.sig().volts() - source.sig().volts() >= VGSTH)
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
    @Override void renderSpecific(GraphicsContext gc) {
        double a = 1.0 / 3.0;
        gc.setFill(COL_LEGS);
        gc.fillPolygon(new double[] {2-a, 2+a, 2},
                       new double[] {1+a, 1+a, 2-a}, 3);
    }
    @Override Color getLegsColour() {
        return COL_LEGS;
    }

    // informative
    @Override public String getPrefixID() {
        return "hn";
    }
    @Override protected String getAttrClassName() {
        return Component.ATTR_NAME_OF_HARD_N;
    }

}
