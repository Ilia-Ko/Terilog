package engine.components.mosfets;

import gui.control.ControlMain;

public abstract class PChannel extends MOSFET {

    private int vgsth;

    PChannel(ControlMain control, int vgsth) {
        super(control);
        this.vgsth = vgsth;
    }

    @Override
    public void simulate() {
        // TODO: implement simulation logic
    }

}
