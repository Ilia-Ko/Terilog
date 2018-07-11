package engine.components.logic.two_arg;

import engine.Circuit;
import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class NCON extends Gate2to1 {

    // initialization
    public NCON(ControlMain control) {
        super(control);
    }
    public NCON(ControlMain control, Element data) {
        super(control, data);
    }

    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.P_CH, 2);
        summary.addMOSFET(Circuit.Summary.HARD, Circuit.Summary.N_CH, 2);
        summary.addResistor(1);
        summary.addInput(LogicLevel.NIL, 1);
    }

    // simulation
    @Override LogicLevel function(LogicLevel a, LogicLevel b) {
        int va = a.volts();
        int vb = b.volts();
        int v;
        if (va == vb) v = va;
        else v = 0;
        v *= -1;
        return LogicLevel.parseValue(v);
    }

}
