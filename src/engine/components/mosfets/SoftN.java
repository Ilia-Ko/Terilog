package engine.components.mosfets;

import engine.LogicLevel;
import gui.control.ControlMain;

public class SoftN extends NChannel {

    public SoftN(ControlMain control) {
        super(control, LogicLevel.SOFT_VOLTAGE);
    }

    @Override protected String getAttrClass() {
        return "soft-n";
    }

}
