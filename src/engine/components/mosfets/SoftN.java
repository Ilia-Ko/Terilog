package engine.components.mosfets;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class SoftN extends NChannel {

    public static final String ATTR_CLASS = "soft-n";

    public SoftN(ControlMain control) {
        super(control, LogicLevel.SOFT_VOLTAGE);
    }
    public SoftN(ControlMain control, Element data) {
        super(control, data, LogicLevel.SOFT_VOLTAGE);
    }

    @Override protected String getAttrClass() {
        return ATTR_CLASS;
    }

}
