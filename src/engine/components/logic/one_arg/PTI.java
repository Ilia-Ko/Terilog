package engine.components.logic.one_arg;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class PTI extends Gate1to1 {

    // initialization
    public PTI(ControlMain control) {
        super(control);
    }
    public PTI(ControlMain control, Element data) {
        super(control, data);
    }

    // simulation
    @Override protected LogicLevel function(LogicLevel a) {
        int v = a.volts();
        v *= -1;
        if (v == 0) v = +1;
        return LogicLevel.parseValue(v);
    }

}
