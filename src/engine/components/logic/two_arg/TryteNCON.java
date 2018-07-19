package engine.components.logic.two_arg;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class TryteNCON extends TryteGate2to1 {

    // initialization
    public TryteNCON(ControlMain control) {
        super(control);
    }
    public TryteNCON(ControlMain control, Element data) {
        super(control, data);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        return NCON.func(a, b);
    }

}
