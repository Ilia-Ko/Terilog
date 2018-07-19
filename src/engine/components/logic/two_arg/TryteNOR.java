package engine.components.logic.two_arg;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class TryteNOR extends TryteGate2to1 {

    // initialization
    public TryteNOR(ControlMain control) {
        super(control);
    }
    public TryteNOR(ControlMain control, Element data) {
        super(control, data);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        return NOR.func(a, b);
    }

}
