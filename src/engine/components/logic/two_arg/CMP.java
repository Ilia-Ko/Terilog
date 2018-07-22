package engine.components.logic.two_arg;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.logic.one_arg.STI;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class CMP extends Gate2to1 {

    public CMP(ControlMain control) {
        super(control);
    }
    public CMP(ControlMain control, Element data) {
        super(control, data);
    }

    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        return func(a, b);
    }
    static LogicLevel func(LogicLevel a, LogicLevel b) {
        return NANY.func(LogicLevel.parseValue(-a.volts()), b);
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        NANY.countdown(summary);
        STI.countdown(summary);
    }

}
