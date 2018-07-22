package engine.components.logic.two_arg;

import engine.Circuit;
import engine.LogicLevel;
import engine.components.logic.one_arg.STI;
import engine.components.lumped.Reconciliator;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class MUL extends Gate2to1 {

    // initialization
    public MUL(ControlMain control) {
        super(control);
    }
    public MUL(ControlMain control, Element data) {
        super(control, data);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        return LogicLevel.parseValue(a.volts() * b.volts());
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 4);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 4);
        summary.addInput(LogicLevel.NEG, 1);
        summary.addInput(LogicLevel.POS, 1);
        STI.countdown(summary);
        STI.countdown(summary);
        Reconciliator.countdown(summary);
    }

}
