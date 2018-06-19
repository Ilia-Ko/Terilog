package engine.components.mosfets;

import engine.LogicLevel;
import gui.control.ControlMain;

public class HardP extends PChannel {

    public HardP(ControlMain control) {
        super(control, LogicLevel.HARD_VOLTAGE);
    }

    @Override protected String getAttrClass() {
        return "hard-p";
    }

}
