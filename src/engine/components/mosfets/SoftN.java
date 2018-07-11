package engine.components.mosfets;

import engine.Circuit;
import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class SoftN extends NChannel {

    public SoftN(ControlMain control) {
        super(control, LogicLevel.SOFT_VOLTAGE);
    }
    public SoftN(ControlMain control, Element data) {
        super(control, data, LogicLevel.SOFT_VOLTAGE);
    }

    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.addMOSFET(Circuit.Summary.SOFT, Circuit.Summary.N_CH, 1);
    }

}
