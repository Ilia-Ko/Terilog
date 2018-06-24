package engine.components.logic.inverters;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class PTI extends Inverter {

    // initialization
    public PTI(ControlMain control) {
        super(control);
    }
    public PTI(ControlMain control, Element data) {
        super(control, data);
    }

    // simulation
    @Override protected LogicLevel invert(LogicLevel a) {
        if (a == LogicLevel.POS) return LogicLevel.NEG;
        else if (a == LogicLevel.NIL) return LogicLevel.POS;
        else if (a == LogicLevel.NEG) return LogicLevel.POS;
        else return LogicLevel.ERR;
    }

}
