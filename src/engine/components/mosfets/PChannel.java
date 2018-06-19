package engine.components.mosfets;

import gui.control.ControlMain;
import org.w3c.dom.Element;

public abstract class PChannel extends MOSFET {

    private int vgsth;

    PChannel(ControlMain control, int vgsth) {
        super(control);
        this.vgsth = vgsth;
    }
    PChannel(ControlMain control, Element data, int vgsth) {
        super(control, data);
        this.vgsth = vgsth;
    }

    @Override
    public void simulate() {
        // TODO: implement simulation logic
    }

}
