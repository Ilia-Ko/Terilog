package engine.components.mosfets;

import engine.LogicLevel;
import gui.control.ControlMain;
import org.w3c.dom.Element;

public class SoftP extends PChannel {

    public static final String ATTR_CLASS = "soft-p";

    public SoftP(ControlMain control) {
        super(control, LogicLevel.SOFT_VOLTAGE);
    }
    public SoftP(ControlMain control, Element data) {
        super(control, data, LogicLevel.SOFT_VOLTAGE);
    }

    @Override protected String getAttrClass() {
        return ATTR_CLASS;
    }

}
