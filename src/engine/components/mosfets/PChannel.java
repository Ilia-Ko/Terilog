package engine.components.mosfets;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

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

    @Override
    public void simulate() {
        LogicLevel g = gate.query();
        LogicLevel s = source.query();

        if (s == LogicLevel.ZZZ)
            drain.announce(LogicLevel.ZZZ);
        else if (s == LogicLevel.ERR || g.isUnstable())
            drain.announce(LogicLevel.ERR);
        else if (g.volts() - s.volts() <= vgsth)
            drain.announce(s);
        else
            drain.announce(LogicLevel.ZZZ);
    }

}
