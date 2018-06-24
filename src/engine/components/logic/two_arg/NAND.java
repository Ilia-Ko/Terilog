package engine.components.logic.two_arg;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class NAND extends Gate2to1 {

    // initialization
    public NAND(ControlMain control) {
        super(control);
    }
    public NAND(ControlMain control, Element data) {
        super(control, data);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        int va = a.volts();
        int vb = b.volts();
        int v = Math.min(va, vb);
        v *= -1;
        return LogicLevel.parseValue(v);
    }

}
