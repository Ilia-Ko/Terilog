package engine.components.logic.one_arg;

import engine.Circuit;
import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class PTI extends Gate1to1 {

    // initialization
    public PTI(ControlMain control) {
        super(control);
    }
    public PTI(ControlMain control, Element data) {
        super(control, data);
    }

    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.P_CH, 1);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 1);
    }

    // simulation
    @Override protected LogicLevel function(LogicLevel a) {
        int v = a.volts();
        v *= -1;
        if (v == 0) v = +1;
        return LogicLevel.parseValue(v);
    }

}
