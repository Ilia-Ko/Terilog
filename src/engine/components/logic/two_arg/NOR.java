package engine.components.logic.two_arg;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class NOR extends Gate2to1 {

    // initialization
    public NOR(ControlMain control) {
        super(control);
    }
    public NOR(ControlMain control, Element data) {
        super(control, data);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        int va = a.volts();
        int vb = b.volts();
        int v = Math.max(va, vb);
        v *= -1;
        return LogicLevel.parseValue(v);
    }

}
