package engine.components.lumped;

import engine.LogicLevel;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Node;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

public class Diode extends Component {

    private Pin anode, cathode;

    // initialization
    public Diode(ControlMain control) {
        super(control);
    }
    public Diode(ControlMain control, Element data) {
        super(control, data);
    }
    @Override protected Pin[] initPins() {
        anode = new Pin(this, true, 0, 1);
        cathode = new Pin(this, true, 4, 1);
        return new Pin[] {anode, cathode};
    }

    // simulation
    @Override public HashSet<Node> simulate() {
        HashSet<Node> affected = new HashSet<>();
        boolean changedA = false, changedC = false;
        LogicLevel a = anode.query();
        LogicLevel c = cathode.query();

        // simulate
        if (a == LogicLevel.ERR || c == LogicLevel.ERR) {
            changedA = anode.update(LogicLevel.ERR);
            changedC = cathode.update(LogicLevel.ERR);
        } else if (a == LogicLevel.ZZZ && c == LogicLevel.NEG)
            changedA = anode.update(LogicLevel.NEG);
        else if (a == LogicLevel.POS && c == LogicLevel.ZZZ)
            changedC = cathode.update(LogicLevel.POS);
        else if (a == LogicLevel.POS && c == LogicLevel.NEG) {
            changedA = anode.update(LogicLevel.ERR);
            changedC = cathode.update(LogicLevel.ERR);
        }

        // report about affected nodes
        if (changedA) affected.add(anode.gather());
        if (changedC) affected.add(cathode.gather());
        return affected;
    }

}
