package engine.transistors;

import engine.Component;
import engine.LogicLevel;
import engine.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class SoftN extends MOSFET {

    public static final String ATTR_CLASS_NAME = "soft-n";

    private static final Color COL_LEGS = Color.DARKBLUE;
    private static final int VGSTH = 1;

    @Override public Component newCompOfTheSameClass() {
        return new SoftN();
    }

    // simulation
    @Override public ArrayList<Node> simulate() {
        // compute output signal
        LogicLevel out;
        if (gate.sig() == LogicLevel.ZZZ && source.sig() == LogicLevel.ZZZ)
            out = LogicLevel.ZZZ;
        else if (gate.sig().isUnstable() || source.sig().isUnstable())
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
        gc.setStroke(COL_LEGS);
        gc.fillPolygon(new double[] {2-a, 2+a, 2},
                       new double[] {1+a, 1+a, 2-a}, 3);
        gc.strokeOval(1-a, a, 2+a*2, a*4);
    }
    @Override Color getLegsColour() {
        return COL_LEGS;
    }

    // informative

    @Override
    public String toString() {
        return "Soft N";
    }
    @Override public String getPrefixID() {
        return "sn";
    }
    @Override protected String getAttrClassName() {
        return ATTR_CLASS_NAME;
    }

}
