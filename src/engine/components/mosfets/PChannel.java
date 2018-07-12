package engine.components.mosfets;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

import static engine.LogicLevel.*;

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

    @Override public void simulate() {
        LogicLevel g = gate.get();
        LogicLevel s = source.get();
        LogicLevel d = drain.get();
        boolean opened = s.volts() - g.volts() >= vgsth;

        if (s == ZZZ && d == ZZZ) {
            // do nothing
        } else if (g == ERR || (g == ZZZ && (s.isStable() || d.isStable()))) {
            source.put(ERR);
            drain.put(ERR);
        } else if (s.isStable() && (opened || s.volts() < NIL.volts())) {
            drain.put(s);
        } else if (d.isStable() && s.isUnstable() && d.volts() > NIL.volts()) {
            source.put(d);
        } else if (opened && s != d) {
            source.put(ERR);
            drain.put(ERR);
        } else if (!opened) {
            drain.put(ZZZ);
        }
    }

}
