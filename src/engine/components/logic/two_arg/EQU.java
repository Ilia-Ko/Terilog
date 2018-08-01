package engine.components.logic.two_arg;

import engine.Circuit;
import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class EQU extends Gate2to1 {

    public EQU(ControlMain control) {
        super(control);
    }
    public EQU(ControlMain control, Element data) {
        super(control, data);
    }

    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        if (a == b) return LogicLevel.POS;
        return LogicLevel.NIL;
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 5);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 4);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, 2);
        summary.addInput(LogicLevel.NEG, 1);
        summary.addInput(LogicLevel.NIL, 1);
        summary.addInput(LogicLevel.POS, 1);
        summary.addResistor(6);
    }

}
