package engine.components.mosfets;

import engine.LogicLevel;
import gui.control.ControlMain;

public class SoftP extends PChannel {

    public SoftP(ControlMain control) {
        super(control, LogicLevel.SOFT_VOLTAGE);
    }

    @Override protected String getAttrClass() {
        return "soft-p";
    }

}
