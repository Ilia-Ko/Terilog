package engine.components.logic.one_arg;

import engine.Circuit;
import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class NTI extends Gate1to1 {

    // initialization
    public NTI(ControlMain control) {
        super(control);
    }
    public NTI(ControlMain control, Element data) {
        super(control, data);
    }

    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        countdown(summary);
    }
    public static void countdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 1);
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, 1);
    }

    // simulation
    @Override protected LogicLevel function(LogicLevel a) {
        int v = a.volts();
        v *= -1;
        if (v == 0) v = -1;
        return LogicLevel.parseValue(v);
    }

}
