package engine.components.mosfets;

import engine.LogicLevel;
import gui.control.ControlMain;

public class HardN extends NChannel {

    public HardN(ControlMain control) {
        super(control, LogicLevel.HARD_VOLTAGE);
    }

    @Override protected String getAttrClass() {
        return "hard-n";
    }

}
