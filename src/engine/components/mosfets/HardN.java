package engine.components.mosfets;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class HardN extends NChannel {

    public static final String ATTR_CLASS = "hard-n";

    public HardN(ControlMain control) {
        super(control, LogicLevel.HARD_VOLTAGE);
    }
    public HardN(ControlMain control, Element data) {
        super(control, data, LogicLevel.HARD_VOLTAGE);
    }

    @Override protected String getAttrClass() {
        return ATTR_CLASS;
    }

}
