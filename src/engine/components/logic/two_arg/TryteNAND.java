package engine.components.logic.two_arg;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class TryteNAND extends TryteGate2to1 {

    // initialization
    public TryteNAND(ControlMain control) {
        super(control);
    }
    public TryteNAND(ControlMain control, Element data) {
        super(control, data);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        return NAND.func(a, b);
    }

}
