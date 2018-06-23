package engine.components.mosfets;

import engine.LogicLevel;
import engine.connectivity.Node;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

abstract class NChannel extends MOSFET {

    private final int vgsth;

    NChannel(ControlMain control, int vgsth) {
        super(control);
        this.vgsth = vgsth;
    }
    NChannel(ControlMain control, Element data, int vgsth) {
        super(control, data);
        this.vgsth = vgsth;
    }

    @Override public HashSet<Node> simulate() {
        HashSet<Node> affected = new HashSet<>();
        boolean changed;
        LogicLevel g = gate.query();
        LogicLevel s = source.query();

        // simulation
        if (s == LogicLevel.ZZZ)
            changed = drain.update(LogicLevel.ZZZ);
        else if (s == LogicLevel.ERR || g.isUnstable())
            changed = drain.update(LogicLevel.ERR);
        else if (g.volts() - s.volts() >= vgsth)
            changed = drain.update(s);
        else
            changed = drain.update(LogicLevel.ZZZ);

        // report about affected node
        if (changed) affected.add(drain.getNode());
        return affected;
    }

}
