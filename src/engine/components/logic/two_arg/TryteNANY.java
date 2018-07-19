package engine.components.logic.two_arg;

import engine.Circuit;
import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class TryteNANY extends TryteGate2to1 {

    // initialization
    public TryteNANY(ControlMain control) {
        super(control);
    }
    public TryteNANY(ControlMain control, Element data) {
        super(control, data);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        return NANY.func(a, b);
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        TryteGate2to1.countdown(summary);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.P_CH, 2 * 6);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, 2 * 6);
    }

}
