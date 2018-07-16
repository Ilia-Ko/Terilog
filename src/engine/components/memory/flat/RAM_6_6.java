package engine.components.memory.flat;

import gui.control.ControlMain;
import org.w3c.dom.Element;

public class RAM_6_6 extends Flat {

    public RAM_6_6(ControlMain control) {
        super(control, 6, 6);
    }
    public RAM_6_6(ControlMain control, Element data) {
        super(control, data, 6, 6);
    }

}
