package engine.lumped;

import engine.Component;
import engine.LogicLevel;
import engine.Node;
import gui.control.ControlMain;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Diode extends Component {

    public static final String ATTR_CLASS_NAME = "diode";
    private static final int WIDTH = 4;
    private static final int HEIGHT = 2;

    private Pin anode, cathode;

    // initialization
    @Override protected Pin[] initPins() {
        anode = new Pin(this, "anode");
        cathode = new Pin(this, "cathode");
        anode.setPos(4, 1);
        cathode.setPos(0, 1);
        return new Pin[] {anode, cathode};
    }
    @Override public Component newCompOfTheSameClass() {
        return new Diode();
    }

    // simulation
    @Override public ArrayList<Node> simulate() {
        // compute signals
        if (anode.sig() == LogicLevel.ERR || cathode.sig() == LogicLevel.ERR) {
            anode.sendSig(LogicLevel.ERR);
            cathode.sendSig(LogicLevel.ERR);
        } else if (anode.sig() == LogicLevel.POS && cathode.sig() == LogicLevel.NEG) {
            anode.sendSig(LogicLevel.ERR);
            cathode.sendSig(LogicLevel.ERR);
        } else if (anode.sig() == LogicLevel.NEG && cathode.sig() == LogicLevel.POS) {
            anode.sendSig(LogicLevel.ZZZ);
            cathode.sendSig(LogicLevel.ZZZ);
        } else if (anode.sig() == LogicLevel.ZZZ && cathode.sig() == LogicLevel.NEG) {
            anode.sendSig(LogicLevel.NEG);
        } else if (anode.sig() == LogicLevel.POS && cathode.sig() == LogicLevel.ZZZ) {
            cathode.sendSig(LogicLevel.POS);
        }

        // affected nodes
        ArrayList<Node> affected = new ArrayList<>();
        affected.add(anode.getNode());
        affected.add(cathode.getNode());
        return affected;
    }
    @Override protected boolean isIndependent() {
        return false;
    }

    // rendering
    @Override protected void renderBody(GraphicsContext gc) {
        gc.setLineWidth(ControlMain.LINE_WIDTH);
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);

        // anode
        gc.strokeLine(0, 1, 1, 1);

        // body
        gc.fillPolygon(new double[] {1, 3, 1},
                       new double[] {0, 1, 2}, 3);
        gc.strokeLine(3, 0, 3, 2);

        // cathode
        gc.strokeLine(3, 1, 4, 1);
    }
    @Override protected int getAbsoluteWidth() {
        return WIDTH;
    }
    @Override protected int getAbsoluteHeight() {
        return HEIGHT;
    }

    // informative
    @Override public String toString() {
        return "Diode";
    }
    @Override protected String getAttrClassName() {
        return ATTR_CLASS_NAME;
    }
    @Override public String getPrefixID() {
        return "d";
    }

}
