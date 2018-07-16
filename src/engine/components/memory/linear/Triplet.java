package engine.components.memory.linear;

import gui.control.ControlMain;
import org.w3c.dom.Element;

public class Triplet extends Linear {

    public Triplet(ControlMain control) {
        super(control, 3);
    }
    public Triplet(ControlMain control, Element data) {
        super(control, data, 3);
    }

}
