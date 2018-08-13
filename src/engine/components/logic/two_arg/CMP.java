package engine.components.logic.two_arg;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.logic.one_arg.STI;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class CMP extends Gate2to1 {

    // initialization
    public CMP(ControlMain control) {
        super(control);
    }
    public CMP(ControlMain control, Element data) {
        super(control, data);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        return NANY.func(LogicLevel.parseValue(-a.volts()), b);
    }
    @Override protected void singleCountdown(Circuit.Summary summary) {
        NANY.countdown(summary);
        STI.countdown(summary);
    }

}
