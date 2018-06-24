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
    @Override protected HashSet<Pin> initPins() {
        anode = new Pin(this, true, true, 0, 1);
        cathode = new Pin(this, true, true, 4, 1);
        HashSet<Pin> pins = new HashSet<>();
        pins.add(anode);
        pins.add(cathode);
        return pins;
    }

    // simulation
    @Override public HashSet<Node> simulate() {
        boolean changedA = false, changedC = false;
        LogicLevel a = anode.querySigFromNode();
        LogicLevel c = cathode.querySigFromNode();

        // simulate
        if (a == LogicLevel.ERR || c == LogicLevel.ERR) {
            changedA = anode.update(LogicLevel.ERR);
            changedC = cathode.update(LogicLevel.ERR);
        } else if (a == LogicLevel.ZZZ && c == LogicLevel.NEG)
            changedA = anode.update(LogicLevel.NEG);
        else if (a == LogicLevel.POS && c == LogicLevel.ZZZ)
            changedC = cathode.update(LogicLevel.POS);
        else if (a.volts() > c.volts()) {
            changedA = anode.update(LogicLevel.ERR);
            changedC = cathode.update(LogicLevel.ERR);
        }

        // report about affected nodes
        HashSet<Node> affected = new HashSet<>();
        if (changedA) affected.add(anode.getNode());
        if (changedC) affected.add(cathode.getNode());
        return affected;
    }

}
