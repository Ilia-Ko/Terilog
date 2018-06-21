package engine.components.mosfets;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class SoftP extends PChannel {

    public SoftP(ControlMain control) {
        super(control, LogicLevel.SOFT_VOLTAGE);
    }
    public SoftP(ControlMain control, Element data) {
        super(control, data, LogicLevel.SOFT_VOLTAGE);
    }

}
