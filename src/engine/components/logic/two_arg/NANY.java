package engine.components.logic.two_arg;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class NANY extends Gate2to1 {

    // initialization
    public NANY(ControlMain control) {
        super(control);
    }
    public NANY(ControlMain control, Element data) {
        super(control, data);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        int va = a.volts();
        int vb = b.volts();
        int v;
        if (va * vb == 0) v = va + vb;
        else if (va == vb) v = va;
        else v = 0;
        v *= -1;
        return LogicLevel.parseValue(v);
    }

}