package engine.components.mosfets;

import engine.LogicLevel;
import engine.connectivity.Node;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import java.util.HashSet;

abstract class PChannel extends MOSFET {

    private final int vgsth;

    PChannel(ControlMain control, int vgsth) {
        super(control);
        this.vgsth = vgsth;
    }
    PChannel(ControlMain control, Element data, int vgsth) {
        super(control, data);
        this.vgsth = vgsth;
    }

    @Override public HashSet<Node> simulate() {
        HashSet<Node> affected = new HashSet<>();
        boolean changed;
        LogicLevel g = gate.query();
        LogicLevel s = source.query();

        // simulate
        if (s == LogicLevel.ZZZ)
            changed = drain.update(LogicLevel.ZZZ);
        else if (s == LogicLevel.ERR || g.isUnstable())
            changed = drain.update(LogicLevel.ERR);
        else if (g.volts() - s.volts() <= vgsth)
            changed = drain.update(s);
        else
            changed = drain.update(LogicLevel.ZZZ);

        // report about affected nodes
        if (changed) affected.add(drain.gather());
        return affected;
    }

}
